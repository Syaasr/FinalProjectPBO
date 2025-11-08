package com.dapasril.finalprojectpbo.entity;// Ganti dengan package Anda

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;

/**
 * Kelas Missile yang dimodifikasi untuk berbelok secara halus (homing missile).
 * * Perubahan Utama:
 * 1. Constructor: Kini juga menetapkan 'velocity' awal berdasarkan helicopterTransform.
 * 2. Variabel baru: 'turnSpeed' untuk mengontrol seberapa cepat misil berbelok.
 * 3. update(): Menggunakan Vector3.lerp() untuk mengubah 'velocity' secara bertahap.
 */
public class Missile {
    public ModelInstance instance;
    public Vector3 velocity = new Vector3(); // Vektor kecepatan SAAT INI
    public float lifeTimer = 0f;
    public ModelInstance target; // Target yang akan dikejar
    public boolean isAlive = true;

    // --- PENGATURAN MISIL ---
    public static float missileSpeed = 90f;   // Kecepatan jelajah misil
    public static float missileLifetime = 3f; // Waktu hidup misil (3 detik)
    
    /**
     * KUNCI DARI PERMINTAAN ANDA: Kecepatan Belok.
     * Mengontrol seberapa "halus" belokannya.
     * - Nilai kecil (misal 1.0f): Belokan lambat & lebar (seperti kapal tanker).
     * - Nilai besar (misal 5.0f): Belokan cepat & tajam.
     * - Nilai sangat besar (misal 100.0f): Akan jadi instan (seperti kode saya sebelumnya).
     * Mari kita mulai dengan 2.0f.
     */
    public static float turnSpeed = 2.0f;

    // --- VARIABEL TEMPORARY UNTUK KALKULASI (MENGHINDARI GARBAGE COLLECTION) ---
    
    // Asumsi model 3D misil Anda dibuat menghadap sumbu Z positif (0, 0, 1)
    // Sesuaikan ini jika model Anda menghadap arah lain (misal: (0, 0, -1) atau (1, 0, 0))
    private static final Vector3 MODEL_FORWARD = new Vector3(0, 0, 1);
    
    // Variabel pembantu untuk perhitungan di dalam update()
    private static final Vector3 missilePos = new Vector3();
    private static final Vector3 targetPos = new Vector3();
    private static final Vector3 idealDirection = new Vector3();
    private static final Vector3 idealVelocity = new Vector3();
    private static final Quaternion rotation = new Quaternion();


    /**
     * CONSTRUCTOR
     * * @param model Model 3D untuk misil.
     * @param helicopterTransform Transform dari helikopter (untuk posisi & rotasi spawn).
     * @param target ModelInstance target yang akan dikejar.
     */
    public Missile(Model model, Matrix4 helicopterTransform, ModelInstance target) {
        this.instance = new ModelInstance(model);
        this.target = target;
        this.isAlive = true;
        this.lifeTimer = 0f;

        // 1. Atur posisi dan rotasi misil sama dengan helikopter (titik spawn)
        this.instance.transform.set(helicopterTransform);

        // 2. INI PENTING: Atur kecepatan AWAL misil.
        // Ini adalah implementasi dari "launch dulu forward".
        // Misil akan mulai bergerak lurus ke depan dari helikopter.
        // Kita ambil arah Z+ (0,0,1) dan rotasikan sesuai arah helikopter.
        this.velocity.set(0, 0, 1f).rot(helicopterTransform).scl(missileSpeed);
    }

    /**
     * METODE UPDATE
     * Logika utama untuk "mengikuti secara halus" ada di sini.
     * Metode ini harus dipanggil setiap frame dari game loop utama Anda.
     * * @param delta Waktu (detik) sejak frame terakhir.
     */
    public void update(float delta) {
        // Jangan update jika misil sudah "mati" atau tidak punya target
        if (!isAlive || target == null) {
            return;
        }

        // 1. Update waktu hidup
        lifeTimer += delta;
        if (lifeTimer > missileLifetime) {
            isAlive = false; // Misil "mati" setelah waktunya habis
            return;
        }

        // 2. Dapatkan posisi misil saat ini dan posisi target
        instance.transform.getTranslation(missilePos);
        target.transform.getTranslation(targetPos);

        // 3. Hitung arah IDEAL (arah sempurna) dari misil ke target
        idealDirection.set(targetPos).sub(missilePos).nor();

        // 4. Hitung kecepatan IDEAL (kecepatan sempurna)
        // Ini adalah vektor kecepatan yang kita INGINKAN
        idealVelocity.set(idealDirection).scl(missileSpeed);

        // 5. INI BAGIAN UTAMANYA: "Berbelok Halus"
        // Kita tidak langsung mengatur velocity = idealVelocity.
        // Kita "lerp" (interpolasi linear) velocity SAAT INI menuju velocity IDEAL.
        // 'delta * turnSpeed' mengontrol seberapa cepat interpolasi ini terjadi.
        velocity.lerp(idealVelocity, delta * turnSpeed);

        // 6. Hitung posisi baru misil menggunakan velocity yang sudah di-lerp
        // newPosition = currentPosition + (currentVelocity * delta)
        missilePos.add(velocity.cpy().scl(delta));

        // 7. Hitung rotasi baru agar misil "menghadap" ke arah pergerakannya
        // Kita gunakan .nor() pada velocity untuk mendapatkan arah murni
        rotation.setFromCross(MODEL_FORWARD, velocity.cpy().nor());

        // 8. Terapkan posisi DAN rotasi baru ke transform misil
        instance.transform.set(missilePos, rotation);
    }
}