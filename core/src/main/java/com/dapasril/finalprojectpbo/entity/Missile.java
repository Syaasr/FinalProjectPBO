package com.dapasril.finalprojectpbo.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;

public class Missile {
	public ModelInstance instance;
    public Vector3 velocity = new Vector3();
    public float lifeTimer = 0f;
    public ModelInstance target;
    public boolean isAlive = true;

    public static float missileSpeed = 90f;
    public static float missileLifetime = 3f;
    
    public static float turnSpeed = 2.0f;

    private static final Vector3 MODEL_FORWARD = new Vector3(0, 0, 1);
    
    private static final Vector3 missilePos = new Vector3();
    private static final Vector3 targetPos = new Vector3();
    private static final Vector3 idealDirection = new Vector3();
    private static final Vector3 idealVelocity = new Vector3();
    private static final Quaternion rotation = new Quaternion();

    public Missile(Model model, Matrix4 helicopterTransform, ModelInstance target) {
        this.instance = new ModelInstance(model);
        this.target = target;
        this.isAlive = true;
        this.lifeTimer = 0f;

        this.instance.transform.set(helicopterTransform);

        this.velocity.set(0, 0, 1f).rot(helicopterTransform).scl(missileSpeed);
    }

    public void update(float delta) {
        if (!isAlive || target == null) {
            return;
        }

        lifeTimer += delta;
        if (lifeTimer > missileLifetime) {
            isAlive = false;
            return;
        }

        instance.transform.getTranslation(missilePos);
        target.transform.getTranslation(targetPos);

        idealDirection.set(targetPos).sub(missilePos).nor();
        idealVelocity.set(idealDirection).scl(missileSpeed);

        velocity.lerp(idealVelocity, delta * turnSpeed);

        missilePos.add(velocity.cpy().scl(delta));

        rotation.setFromCross(MODEL_FORWARD, velocity.cpy().nor());

        instance.transform.set(missilePos, rotation);
    }
}