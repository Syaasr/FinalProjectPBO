package com.dapasril.finalprojectpbo.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Missile {
	public ModelInstance instance;
	public Vector3 velocity = new Vector3();
	public float lifeTimer = 0f;
	public static float missileSpeed = 50f; // Kecepatan misil
	public static float missileLifetime = 4f; // Waktu hidup misil (4 detik)
	
	public Missile(Model model, Matrix4 helicopterTransform) {
		instance = new ModelInstance(model);
		// Atur posisi dan rotasi misil sama dengan helikopter
		instance.transform.set(helicopterTransform);
	
		// Hitung vektor kecepatan
		// Kita gunakan .rot(helicopterTransform) agar konsisten dengan
		// cara gerak maju helikopter (moveVector.rot(heliRoot))
	    velocity.set(0, 0, 1f).rot(helicopterTransform).scl(missileSpeed);
	}
}
