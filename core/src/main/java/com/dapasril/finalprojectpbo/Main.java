package com.dapasril.finalprojectpbo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion; // BARU
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import java.util.Iterator;

import com.dapasril.finalprojectpbo.entity.Missile;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    public PerspectiveCamera cam;
    public Environment environment;
    public boolean mouseCatched;

    public ModelBatch modelBatch;
    public AssetManager assets;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public boolean loading;

    // Heli
    Matrix4 heliRoot = new Matrix4().setToTranslation(0, 0, 0);
    Matrix4 heliBodyLocal = new Matrix4();
    Matrix4 heliPropLocal = new Matrix4();
    ModelInstance heliBodyInstance, heliPropInstance;
    float rotorSpeed = 25f;

    // Camera Controls
    Matrix4 cameraRoot = new Matrix4().setToTranslation(0, 0, 0);
    Matrix4 cameraPositionLocal = new Matrix4();
    Vector3 camPivot = new Vector3();
    float camDistanceFromTarget = 17f;
    private float yaw = 0;
    private float pitch = 20;
    private float minZoom = 5f; // Batas zoom paling dekat
    private float maxZoom = 50f; // Batas zoom paling jauh

    private Vector3 cameraTarget = new Vector3();
    private Quaternion heliRotation = new Quaternion();
    private Vector3 playerPos = new Vector3();
    private float smoothnessFollow = 5f;
    private float smoothnessRecenter = 3f;
    private Vector3 moveVector = new Vector3();

    // World
    ModelInstance island1Instance, water1Instance;

    // === MULAI VARIABEL BARU MISIL ===
    Model missileModel;
    Array<Missile> activeMissiles = new Array<Missile>();
    // === SELESAI VARIABEL BARU MISIL ===

    @Override
    public void create() {
        Bullet.init();

        modelBatch = new ModelBatch();

        // === MULAI KODE BUAT MODEL MISIL ===
        ModelBuilder modelBuilder = new ModelBuilder();
        missileModel = modelBuilder.createCapsule(0.1f, 1f, 16,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            Usage.Position | Usage.Normal);
        // === SELESAI KODE BUAT MODEL MISIL ===

        // Creating Camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 5f, -10f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        // Creating the Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // ModelBuilder modelBuilder = new ModelBuilder();

        assets = new AssetManager();

        // Loading the Heli67
        assets.load("gta3/heli67/heli67_body.g3db", Model.class);
        assets.load("gta3/heli67/heli67_prop.g3db", Model.class);

        // Loading the world
        assets.load("gta3/island1/island1.g3db", Model.class);
        assets.load("gta3/water1/water1.g3db", Model.class);

        // Loading Barrel1
        assets.load("gta3/barrel1/barrel.g3db", Model.class);

        loading = true;

        Gdx.input.setCursorCatched(mouseCatched);
    }

    private void doneLoading() {

        // Getting Heli67
        Model heliBodyModel = assets.get("gta3/heli67/heli67_body.g3db", Model.class);
        Model heliPropModel = assets.get("gta3/heli67/heli67_prop.g3db", Model.class);

        heliBodyInstance = new ModelInstance(heliBodyModel);
        heliPropInstance = new ModelInstance(heliPropModel);

        instances.add(heliBodyInstance);
        instances.add(heliPropInstance);

        // Loading the world like island and water ya know
        Model island1Model = assets.get("gta3/island1/island1.g3db", Model.class);
        Model water1Model = assets.get("gta3/water1/water1.g3db", Model.class);

        island1Instance = new ModelInstance(island1Model);
        water1Instance = new ModelInstance(water1Model);

        island1Instance.transform.setToTranslation(0, -40f, 0);
        water1Instance.transform.setToTranslation(0, -40f, 0);

        instances.add(island1Instance);
        instances.add(water1Instance);

        // === MULAI KODE BARU UNTUK BARREL ===

        // 1. Dapatkan model barrel dari assets
        // (Ganti path ini jika Anda meletakkannya di tempat berbeda)
        Model barrelModel = assets.get("gta3/barrel1/barrel.g3db", Model.class);

        int barrelCount = 50; // Jumlah barrel yang ingin Anda buat
        float spreadArea = 200f; // Area penyebaran (200x200 unit, berpusat di 0,0)
        float groundY = -40f; // Ketinggian tanah (harus sama dengan island1)

        for (int i = 0; i < barrelCount; i++) {
            // 2. Dapatkan posisi X dan Z secara acak
            // Kita gunakan MathUtils.random yang sudah di-import
            float x = MathUtils.random(-spreadArea / 2, spreadArea / 2);
            float z = MathUtils.random(-spreadArea / 2, spreadArea / 2);

            // 3. Buat instance barrel baru
            ModelInstance barrelInstance = new ModelInstance(barrelModel);

            // 4. Atur posisinya
            barrelInstance.transform.setToTranslation(x, groundY, z);
            // Angka (2f, 2f, 2f) berarti 2x lebih besar di semua sumbu (X, Y, Z)
            // Ganti angkanya sesuai keinginan Anda.
            barrelInstance.transform.scale(2f, 2f, 2f);

            // 5. Tambahkan ke daftar render
            instances.add(barrelInstance);
        }
        // === SELESAI KODE BARU UNTUK BARREL ===

        heliRoot.getTranslation(cameraTarget);

        // after loading all
        loading = false;
    }

    @Override
    public void render() {
        if(loading && assets.update()) {
            this.doneLoading();
        }

        Gdx.gl.glClearColor(0, 0.25f, 1f, 1f);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float mouseDeltaX = -Gdx.input.getDeltaX();
        float mouseDeltaY = -Gdx.input.getDeltaY();

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            this.mouseCatched = !this.mouseCatched;
            Gdx.input.setCursorCatched(mouseCatched);
        }

        boolean isMovingForward = false;

        if(heliPropInstance != null && heliBodyInstance != null) {
            heliPropLocal.rotate(new Vector3(0, 1f, 0), rotorSpeed);

            heliBodyInstance.transform.set(heliRoot).mul(heliBodyLocal);
            heliPropInstance.transform.set(heliRoot).mul(heliPropLocal);

            // Heli Control
            isMovingForward = Gdx.input.isKeyPressed(Input.Keys.W);
            if(isMovingForward) {
                moveVector.set(0, 0.1f, 0.2f);
                moveVector.rot(heliRoot);
                heliRoot.trn(moveVector);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.S)) {
                moveVector.set(0, -0.05f, 0);
                moveVector.rot(heliRoot);
                heliRoot.trn(moveVector);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.A)) {
                heliRoot.rotate(new Vector3(0, 0, -1f), 1.5f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.D)) {
                heliRoot.rotate(new Vector3(0, 0, 1f), 1.5f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                heliRoot.rotate(new Vector3(0, 1f, 0), 2f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                heliRoot.rotate(new Vector3(0, 1f, 0), -2f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
                heliRoot.rotate(new Vector3(1f, 0, 0), 2f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                heliRoot.rotate(new Vector3(1f, 0, 0), -2f);
            }

            float zoomSpeed = 0.2f;
            if(Gdx.input.isKeyPressed(Input.Keys.I)) {
                camDistanceFromTarget -= zoomSpeed; // Zoom In
            }
            if(Gdx.input.isKeyPressed(Input.Keys.O)) {
                camDistanceFromTarget += zoomSpeed; // Zoom Out
            }

            camDistanceFromTarget = MathUtils.clamp(camDistanceFromTarget, minZoom, maxZoom);

            heliRoot.getTranslation(playerPos);

            heliRoot.getRotation(heliRotation); // PASTIKAN BARIS INI ADA DI SINI

            // === MULAI KODE TEMBAK MISIL ===
            if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                Missile missile = new Missile(missileModel, heliRoot); // UBAH MENJADI INI
                activeMissiles.add(missile);
                instances.add(missile.instance); // Tambahkan ke list render
            }
            // === SELESAI KODE TEMBAK MISIL ===

            // SMOOTH FOLLOW (Kode ini sudah benar)
            cameraTarget.lerp(playerPos, Gdx.graphics.getDeltaTime() * smoothnessFollow);
        }

        // === MULAI KODE UPDATE MISIL ===
        Iterator<Missile> iter = activeMissiles.iterator();
        while(iter.hasNext()) {
            Missile missile = iter.next();

            // Update timer
            missile.lifeTimer += Gdx.graphics.getDeltaTime();

            // Cek jika misil sudah "habis"
            if(missile.lifeTimer > Missile.missileLifetime) {
                iter.remove(); // Hapus dari list update
                instances.removeValue(missile.instance, true); // Hapus dari list render
            } else {
                // Gerakkan misil
                missile.instance.transform.trn(missile.velocity.x * Gdx.graphics.getDeltaTime(),
                    missile.velocity.y * Gdx.graphics.getDeltaTime(),
                    missile.velocity.z * Gdx.graphics.getDeltaTime());
            }
        }
        // === SELESAI KODE UPDATE MISIL === //


        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();

        camPivot.set(cameraTarget);

        if (mouseDeltaX != 0 || mouseDeltaY != 0) {
            yaw += mouseDeltaX * 0.3f;
            pitch += mouseDeltaY * 0.3f;
        } else if (isMovingForward) {
            float targetYaw = heliRotation.getYaw();
            float cameraTargetYaw = targetYaw + 180f;
            yaw = MathUtils.lerpAngleDeg(yaw, cameraTargetYaw, Gdx.graphics.getDeltaTime() * smoothnessRecenter);
        }


        pitch = MathUtils.clamp(pitch, -80f, 80f);

        float radYaw = MathUtils.degreesToRadians * yaw;
        float radPitch = MathUtils.degreesToRadians * pitch;

        float x = camPivot.x + camDistanceFromTarget * MathUtils.cos(radPitch) * MathUtils.sin(radYaw);
        float y = camPivot.y + camDistanceFromTarget * MathUtils.sin(radPitch);
        float z = camPivot.z + camDistanceFromTarget * MathUtils.cos(radPitch) * MathUtils.cos(radYaw);

        cam.position.set(x, y, z);
        cam.lookAt(camPivot);
        cam.up.set(Vector3.Y);
        cam.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
        missileModel.dispose();
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
