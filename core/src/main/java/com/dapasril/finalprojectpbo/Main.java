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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

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
	
	// World
	ModelInstance island1Instance, water1Instance;
	
    
	@Override
    public void create() {
		
		modelBatch = new ModelBatch();
		
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
    	
    	Vector3 playerPos = new Vector3();
    	
    	if(Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
    		this.mouseCatched = !this.mouseCatched;
    		Gdx.input.setCursorCatched(mouseCatched);
    	}
    	
    	if(heliPropInstance != null && heliBodyInstance != null) {
    		heliPropLocal.rotate(new Vector3(0, 1f, 0), rotorSpeed);
    		
    		heliBodyInstance.transform.set(heliRoot).mul(heliBodyLocal);
    		heliPropInstance.transform.set(heliRoot).mul(heliPropLocal);
    		
    		// Heli Control
    		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
    			heliRoot.translate(0, 0.1f, 0.2f);
    		}
    		
    		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
    			heliRoot.translate(0, -0.05f, 0);
    		}
    		
    		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
    			heliRoot.rotate(new Vector3(0, 0, -1f), 1.5f);
    		}
    		
    		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
    			heliRoot.rotate(new Vector3(0, 0, 1f), 1.5f);
    		}
    		
    		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
    			heliRoot.rotate(new Vector3(1f, 0, 0), 2f);
    		}
    		
    		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
    			heliRoot.rotate(new Vector3(1f, 0, 0), -2f);
    		}
    		
    		heliRoot.getTranslation(playerPos);
    	}
    
    	
    	modelBatch.begin(cam);
    	modelBatch.render(instances, environment);
    	modelBatch.end();
    	
    	// Doing camera stuff prodg
    	camPivot.set(playerPos);
    	yaw += mouseDeltaX * 0.3f;
    	pitch += mouseDeltaY * 0.3f;
    	
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
    }

	@Override
	public void resize(int width, int height) {		
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {		
	}
}
