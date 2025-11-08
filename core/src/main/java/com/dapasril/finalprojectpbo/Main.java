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
import java.util.Iterator;

import com.dapasril.finalprojectpbo.entity.HelicopterGenerator;
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

    // Heli - Now this whole helicopter thing is simpler, right?
    HelicopterGenerator heliPlayer;
    
    // Camera Controls
    Matrix4 cameraRoot = new Matrix4().setToTranslation(0, 0, 0);
    Matrix4 cameraPositionLocal = new Matrix4();
    Vector3 camPivot = new Vector3();
    float camDistanceFromTarget = 17f;
    private float yaw = 0;
    private float pitch = 20;
    private float minZoom = 5f;
    private float maxZoom = 50f;

    private Vector3 cameraTarget = new Vector3();
    private Quaternion playerRot = new Quaternion();
    private Vector3 playerPos = new Vector3();
    private float smoothnessFollow = 5f;
    private float smoothnessRecenter = 3f;
    private Vector3 moveVector = new Vector3();

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

        modelBatch = new ModelBatch();
        
        // 2D
        spriteBatch = new SpriteBatch();
        
        // Fonts
        this.fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pricedown.otf"));
        this.fontsPricedown = new Array<BitmapFont>();
        this.fontParameters = new Array<FreeTypeFontParameter>();
        
        for(int i = 0; i < this.fontSizes; i++) {        	
        	// Setting the parameter
        	this.fontParameters.add(new FreeTypeFontParameter());
        	this.fontParameters.get(i).size = (int)(this.initialFontSize * (1.61803398875f * (i + 1)));
        	this.fontParameters.get(i).borderWidth = 1.5f;
        	this.fontParameters.get(i).borderColor = Color.BLACK;
        	
        	// Setting the fonts
        	this.fontsPricedown.add(fontGenerator.generateFont(this.fontParameters.get(i)));
        }

        // Creating Camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 5f, -10f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 1000f;
        cam.update();

        // Creating the Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // ModelBuilder modelBuilder = new ModelBuilder();

        assets = new AssetManager();
        
        this.heliPlayer = new HelicopterGenerator();

        // Loading the Heli67
        assets.load("gta3/heli67/heli67_body.g3db", Model.class);
        assets.load("gta3/heli67/heli67_prop.g3db", Model.class);

        // Loading the world
        assets.load("gta3/island1/island1.g3db", Model.class);
        assets.load("gta3/water1/water1.g3db", Model.class);

        // Loading Barrel1
        assets.load("gta3/barrel1/barrel1.g3db", Model.class);

        // Loading Missile1
        assets.load("gta3/missile1/missile1.g3db", Model.class);

        loading = true;

        Gdx.input.setCursorCatched(mouseCatched);
    }

    private void doneLoading() {
    	this.heliPlayer.getModels(assets);
    	this.heliPlayer.addToInstances(instances);
    	
        // Setting the world like island and water ya know
        Model island1Model = assets.get("gta3/island1/island1.g3db", Model.class);
        Model water1Model = assets.get("gta3/water1/water1.g3db", Model.class);

        island1Instance = new ModelInstance(island1Model);
        water1Instance = new ModelInstance(water1Model);

        island1Instance.transform.setToTranslation(0, -40f, 0);
        water1Instance.transform.setToTranslation(0, -40f, 0);

        instances.add(island1Instance);
        instances.add(water1Instance);

        // Setting the barrel brother
        Model barrelModel = assets.get("gta3/barrel1/barrel1.g3db", Model.class);

        int barrelCount = 50;
        float spreadArea = 200f;
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
        this.missileModel = assets.get("gta3/missile1/missile1.g3db", Model.class);

        this.heliPlayer.rootTransform.getTranslation(cameraTarget);

        // after loading all
        loading = false;
    }

    @Override
    public void render() {
        if(loading && assets.update()) {
            this.doneLoading();
        }

        Gdx.gl.glClearColor(0.45f, 0.78f, 1f, 1f);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float mouseDeltaX = -Gdx.input.getDeltaX();
        float mouseDeltaY = -Gdx.input.getDeltaY();

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            this.mouseCatched = !this.mouseCatched;
            Gdx.input.setCursorCatched(mouseCatched);
        }

        boolean isMovingForward = false;
        this.heliPlayer.rotorCanMove = true;
        if(this.heliPlayer.nullChecker()) {
        	
        	this.heliPlayer.update();

            // Heli Control
            isMovingForward = Gdx.input.isKeyPressed(Input.Keys.W);
            if(isMovingForward) {
                moveVector.set(0, 0.1f, 0.2f);
                moveVector.rot(this.heliPlayer.rootTransform);
                this.heliPlayer.rootTransform.trn(moveVector);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.S)) {
                moveVector.set(0, -0.05f, 0);
                moveVector.rot(this.heliPlayer.rootTransform);
                this.heliPlayer.rootTransform.trn(moveVector);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.A)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 0, -1f), 1.5f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.D)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 0, 1f), 1.5f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 1f, 0), 2f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(0, 1f, 0), -2f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(1f, 0, 0), 2f);
            }

            if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                this.heliPlayer.rootTransform.rotate(new Vector3(1f, 0, 0), -2f);
            }

            float scrollAmount = Gdx.input.getRoll();
            if (scrollAmount != 0) {
                float scrollSpeed = 1.5f;
                camDistanceFromTarget += scrollAmount * scrollSpeed;
            }

            float zoomSpeed = 0.2f;
            if(Gdx.input.isKeyPressed(Input.Keys.I)) {
                camDistanceFromTarget -= zoomSpeed;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.O)) {
                camDistanceFromTarget += zoomSpeed;
            }

            camDistanceFromTarget = MathUtils.clamp(camDistanceFromTarget, minZoom, maxZoom);
            
            this.heliPlayer.updatePosRot(playerPos, playerRot);

            if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && mouseCatched)) {
                if (currentAmmoInClip > 0 && !isReloading) {
                    Missile missile = new Missile(missileModel, this.heliPlayer.rootTransform);
                    activeMissiles.add(missile);
                    instances.add(missile.instance);
                    currentAmmoInClip--;
                }
            }

            cameraTarget.lerp(playerPos, Gdx.graphics.getDeltaTime() * smoothnessFollow);
        }

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

        Iterator<Missile> iter = activeMissiles.iterator();
        while(iter.hasNext()) {
            Missile missile = iter.next();

            missile.lifeTimer += Gdx.graphics.getDeltaTime();

            if(missile.lifeTimer > Missile.missileLifetime) {
                iter.remove();
                instances.removeValue(missile.instance, true);
            } else {
                missile.instance.transform.trn(missile.velocity.x * Gdx.graphics.getDeltaTime(),
                    missile.velocity.y * Gdx.graphics.getDeltaTime(),
                    missile.velocity.z * Gdx.graphics.getDeltaTime());
            }
        }


        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
        
        // Doing the 2D
        spriteBatch.begin();
        GlyphLayout layout = new GlyphLayout();
        
        String ammoInClipText = Integer.toString(currentAmmoInClip);
        String totalAmmoText = Integer.toString(totalReserveAmmo);

 		float screenWidth = Gdx.graphics.getWidth();
 		
 		layout.setText(fontsPricedown.get(1), ammoInClipText);
 		fontsPricedown.get(1).draw(spriteBatch, ammoInClipText, Gdx.graphics.getWidth() - layout.width - ((10 * totalAmmoText.length()) + this.fontParameters.get(1).size), Gdx.graphics.getHeight() - 10);
 		
 		layout.setText(fontsPricedown.get(1), totalAmmoText);
 		fontsPricedown.get(1).draw(spriteBatch, totalAmmoText, Gdx.graphics.getWidth() - layout.width - 10, Gdx.graphics.getHeight() - 10);

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

        camPivot.set(cameraTarget);

        if (mouseDeltaX != 0 || mouseDeltaY != 0) {
            yaw += mouseDeltaX * 0.3f;
            pitch += mouseDeltaY * 0.3f;
        } else if (isMovingForward) {
            float targetYaw = playerRot.getYaw();
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
