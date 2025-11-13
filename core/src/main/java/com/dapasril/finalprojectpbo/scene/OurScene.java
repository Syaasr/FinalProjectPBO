package com.dapasril.finalprojectpbo.scene;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;

public abstract class OurScene {
    public String name;

    protected Array<ModelInstance> instances;

    public OurScene(String name) {
        this.name = name;
        this.instances = new Array<ModelInstance>();
    }

    public abstract void init();

    public abstract void update();

    public abstract void dispose();
}
