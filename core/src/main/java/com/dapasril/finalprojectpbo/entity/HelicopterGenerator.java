package com.dapasril.finalprojectpbo.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class HelicopterGenerator {
    public final String fuselagePath = "gta3/heli67/heli67_body.g3db";
    public final String rotorPath = "gta3/heli67/heli67_prop.g3db";

    private ModelInstance fuselage, rotor;
    public Matrix4 localFuselage, localRotor;
    public Matrix4 rootTransform;

    public boolean isPlayable;
    public boolean rotorCanMove;
    public float rotorSpeed = 25f;

    public HelicopterGenerator(boolean isPlayable) {
        this.isPlayable = isPlayable;
        this.localFuselage = new Matrix4();
        this.localRotor = new Matrix4();
        this.rootTransform = new Matrix4().setToTranslation(0, 0, 0);
    }

    // For NPCs which is not playable of course
    public HelicopterGenerator() {
        this.isPlayable = false;
        this.localFuselage = new Matrix4();
        this.localRotor = new Matrix4();
        this.rootTransform = new Matrix4().setToTranslation(0, 0, 0);
    }

    public static void loadModels(AssetManager assetManager, HelicopterGenerator heli) {
        assetManager.load(heli.fuselagePath, Model.class);
        assetManager.load(heli.rotorPath, Model.class);
    }

    public void getModels(AssetManager assetManager) {
        this.fuselage = new ModelInstance(assetManager.get(this.fuselagePath, Model.class));
        this.rotor = new ModelInstance(assetManager.get(this.rotorPath, Model.class));
    }

    public void addToInstances(Array<ModelInstance> instances) {
        instances.add(this.fuselage);
        instances.add(this.rotor);
    }

    public ModelInstance getFuselage() {
        return this.fuselage;
    }

    public ModelInstance getRotor() {
        return this.rotor;
    }

    public boolean nullChecker() {
        return this.getRotor() != null && this.getFuselage() != null;
    }

    public void updateTransform() {
        if (this.rotorCanMove) {
            this.localRotor.rotate(new Vector3(0, 1f, 0), this.rotorSpeed);
        }

        this.fuselage.transform.set(this.rootTransform).mul(this.localFuselage);
        this.getRotor().transform.set(this.rootTransform).mul(this.localRotor);
    }

    public void updateInput() {
        if (!this.isPlayable) {
            return;
        }
    }

    public void updatePosRot(Vector3 pos, Quaternion rot) {

        this.rootTransform.getTranslation(pos);
        this.rootTransform.getRotation(rot);
    }
}
