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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.Color;
import java.util.Random;

import com.dapasril.finalprojectpbo.camera.OurThirdPersonCamera;
import com.dapasril.finalprojectpbo.entity.HelicopterGenerator;
import com.dapasril.finalprojectpbo.entity.Missile;
import com.dapasril.finalprojectpbo.scene.OurScene;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main implements ApplicationListener {

    public PerspectiveCamera cam;
    public OurThirdPersonCamera tpsCam;

    public Environment environment;
    public boolean mouseCatched;

    public ModelBatch modelBatch;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public boolean loading;

    // Scene Management
    public Array<OurScene> scenes;
    public int currentSceneIndex = 0;

    // Heli - Now this whole helicopter thing is simpler, right?
    HelicopterGenerator heliPlayer;
    Array<HelicopterGenerator> heliEnemies;
    int enemyCount = 5;

    // Camera
    private Quaternion playerRot = new Quaternion();
    private Vector3 playerPos = new Vector3();

    // World
    ModelInstance island1Instance, water1Instance;

    // Missile
    Model missileModel;
    Array<Missile> activeMissiles = new Array<Missile>();

    // Ammo
    int maxAmmoInClip = 10;
    int currentAmmoInClip = 10;
    int totalReserveAmmo = 50;

    boolean isReloading = false;
    float reloadCooldownTime = 2.0f;
    float currentReloadTimer = 0f;

    // Texts
    SpriteBatch spriteBatch;
    Array<BitmapFont> fontsPricedown;
    Array<FreeTypeFontParameter> fontParameters;
    final int fontSizes = 6;
    final int initialFontSize = 16;
    FreeTypeFontGenerator fontGenerator;

    @Override
    public void create() {
        Bullet.init();

        // 2D
        spriteBatch = new SpriteBatch();

        // Fonts
        this.fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pricedown.otf"));
        this.fontsPricedown = new Array<BitmapFont>();
        this.fontParameters = new Array<FreeTypeFontParameter>();

        for (int i = 0; i < this.fontSizes; i++) {
            // Setting the parameter
            this.fontParameters.add(new FreeTypeFontParameter());
            this.fontParameters.get(i).size = (int) (this.initialFontSize * (1.61803398875f * (i + 1)));
            this.fontParameters.get(i).borderWidth = 1.5f;
            this.fontParameters.get(i).borderColor = Color.BLACK;

            // Setting the fonts
            this.fontsPricedown.add(fontGenerator.generateFont(this.fontParameters.get(i)));
        }

        // 3D
        modelBatch = new ModelBatch();

        // Creating Camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 5f, -10f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 1500f;
        cam.update();

        // Creating the Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        this.heliPlayer = new HelicopterGenerator(true);
        this.heliEnemies = new Array<HelicopterGenerator>();
        for (int i = 0; i < this.enemyCount; i++) {
            this.heliEnemies.add(new HelicopterGenerator(false));
        }

        this.tpsCam = new OurThirdPersonCamera(this.cam, this.heliPlayer.rootTransform);
        Gdx.input.setInputProcessor(tpsCam);

        // Loading the Heli67
        HelicopterGenerator.loadModels(Global.assets, heliPlayer);

        // Loading the world
        Global.assets.load("gta3/island1/island1.g3db", Model.class);
        Global.assets.load("gta3/water1/water1.g3db", Model.class);

        // Loading Barrel1
        Global.assets.load("gta3/barrel1/barrel1.g3db", Model.class);

        // Loading Missile1
        Global.assets.load("gta3/missile1/missile1.g3db", Model.class);

        loading = true;

        Gdx.input.setCursorCatched(mouseCatched);
    }

    private void doneLoading() {

        // Setting up the player
        this.heliPlayer.getModels(Global.assets);
        this.heliPlayer.addToInstances(instances);

        // Setting up the enemies
        for (HelicopterGenerator hg : this.heliEnemies) {
            hg.getModels(Global.assets);
            hg.rotorCanMove = true;
            hg.addToInstances(instances);
            int spreadArea = 300;
            float x = MathUtils.random(-spreadArea / 2, spreadArea / 2);
            float y = MathUtils.random(-10, 20);
            float z = MathUtils.random(-spreadArea / 2, spreadArea / 2);
            hg.rootTransform.setToTranslation(x, y, z);
            hg.updateTransform();
        }

        // Setting the world like island and water ya know
        Model island1Model = Global.assets.get("gta3/island1/island1.g3db", Model.class);
        Model water1Model = Global.assets.get("gta3/water1/water1.g3db", Model.class);

        island1Instance = new ModelInstance(island1Model);
        water1Instance = new ModelInstance(water1Model);

        island1Instance.transform.setToTranslation(0, -40f, 0);
        water1Instance.transform.setToTranslation(0, -40f, 0);

        instances.add(island1Instance);
        instances.add(water1Instance);

        // Setting the barrel brother
        Model barrelModel = Global.assets.get("gta3/barrel1/barrel1.g3db", Model.class);

        int barrelCount = 50;
        float spreadArea = 100f;
        float groundY = -38;

        for (int i = 0; i < barrelCount; i++) {
            float x = MathUtils.random(-spreadArea / 2, spreadArea / 2);
            float z = MathUtils.random(-spreadArea / 2, spreadArea / 2);

            ModelInstance barrelInstance = new ModelInstance(barrelModel);

            barrelInstance.transform.setToTranslation(x, groundY, z);
            barrelInstance.transform.scale(2f, 2f, 2f);

            instances.add(barrelInstance);
        }

        // Setting the missile boom boom 1
        this.missileModel = Global.assets.get("gta3/missile1/missile1.g3db", Model.class);

        loading = false;
    }

    @Override
    public void render() {
        if (loading && Global.assets.update()) {
            this.doneLoading();
        }

        Gdx.gl.glClearColor(0.45f, 0.78f, 1f, 1f);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Global.update();

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            this.mouseCatched = !this.mouseCatched;
            Gdx.input.setCursorCatched(mouseCatched);
        }

        // Start 3D
        boolean isMovingForward = false;
        this.heliPlayer.rotorCanMove = true;

        for (HelicopterGenerator hg : this.heliEnemies) {
            if (hg.nullChecker()) {
                hg.rootTransform.translate(0, 0, 0.15f);
                hg.rootTransform.rotate(Vector3.Y, new Random().nextFloat(0, 0.15f));
                hg.updateTransform();
            }
        }

        if (this.heliPlayer.nullChecker()) {

            this.heliPlayer.updateTransform();

            // Heli Control
            isMovingForward = Gdx.input.isKeyPressed(Input.Keys.W);
            if (isMovingForward) {
                Vector3 moveVec = new Vector3(0, 0.1f, 0.2f);
                moveVec.rot(this.heliPlayer.rootTransform);
                this.heliPlayer.rootTransform.trn(moveVec);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                Vector3 moveVec = new Vector3(0, -0.05f, 0f);
                moveVec.rot(this.heliPlayer.rootTransform);
                this.heliPlayer.rootTransform.trn(moveVec);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 0, -1f), 1.5f);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 0, 1f), 1.5f);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 1f, 0), 2f);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 1f, 0), -2f);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(1f, 0, 0), 2f);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(1f, 0, 0), -2f);
            }

            this.heliPlayer.updatePosRot(playerPos, playerRot);

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                    || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && mouseCatched)) {
                if (currentAmmoInClip > 0 && !isReloading) {
                    Missile missile = new Missile(missileModel, this.heliPlayer.rootTransform,
                            this.heliEnemies.get(new Random().nextInt(this.heliEnemies.size)).getFuselage());
                    activeMissiles.add(missile);
                    instances.add(missile.instance);
                    currentAmmoInClip--;
                }
            }

            this.tpsCam.updateLerp();
        }

        System.out.println(Global.inputVertical);

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            if (!isReloading && currentAmmoInClip < maxAmmoInClip && totalReserveAmmo > 0) {
                isReloading = true;
                currentReloadTimer = 0f;
            }
        }

        if (isReloading) {
            currentReloadTimer += Gdx.graphics.getDeltaTime();
            if (currentReloadTimer >= reloadCooldownTime) {
                isReloading = false;
                int ammoNeeded = maxAmmoInClip - currentAmmoInClip;
                int ammoToReload = Math.min(ammoNeeded, totalReserveAmmo);
                currentAmmoInClip += ammoToReload;
                totalReserveAmmo -= ammoToReload;
            }
        }

        for (Missile missile : activeMissiles) {
            missile.lifeTimer += Gdx.graphics.getDeltaTime();

            if (missile.lifeTimer > Missile.missileLifetime) {
                instances.removeValue(missile.instance, true);
            } else {
                missile.instance.transform.trn(missile.velocity.x * Gdx.graphics.getDeltaTime(),
                        missile.velocity.y * Gdx.graphics.getDeltaTime(),
                        missile.velocity.z * Gdx.graphics.getDeltaTime());
            }

            missile.update(Gdx.graphics.getDeltaTime());
        }

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();

        this.tpsCam.updatePivot();
        cam.up.set(Vector3.Y);
        cam.update();
        // End 3D

        // Doing the 2D
        spriteBatch.begin();
        GlyphLayout layout = new GlyphLayout();

        String ammoInClipText = Integer.toString(currentAmmoInClip);
        String totalAmmoText = Integer.toString(totalReserveAmmo);

        float screenWidth = Gdx.graphics.getWidth();

        layout.setText(fontsPricedown.get(1), ammoInClipText);
        fontsPricedown.get(1).draw(spriteBatch, ammoInClipText,
                Gdx.graphics.getWidth() - layout.width
                        - ((10 * totalAmmoText.length()) + this.fontParameters.get(1).size),
                Gdx.graphics.getHeight() - 10);

        layout.setText(fontsPricedown.get(1), totalAmmoText);
        fontsPricedown.get(1).draw(spriteBatch, totalAmmoText, Gdx.graphics.getWidth() - layout.width - 10,
                Gdx.graphics.getHeight() - 10);

        // float ammoTextX = screenWidth - layout.width - 10;
        float ammoTextY = Gdx.graphics.getHeight() - 10;

        if (isReloading) {
            String reloadText = "RELOADING...";
            layout.setText(fontsPricedown.get(0), reloadText);
            float reloadTextX = screenWidth - layout.width - 10;
            float reloadTextY = ammoTextY - 50;
            fontsPricedown.get(0).draw(spriteBatch, reloadText, reloadTextX, reloadTextY);
        }

        spriteBatch.end();
        // End 2D
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        Global.assets.dispose();
        missileModel.dispose();

        spriteBatch.dispose();
        fontsPricedown.clear();
        fontParameters.clear();
        fontGenerator.dispose();
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();

        if (spriteBatch != null) {
            spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
