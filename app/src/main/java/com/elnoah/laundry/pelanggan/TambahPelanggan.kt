package com.elnoah.laundry.pelanggan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelpelanggan
import com.google.firebase.database.FirebaseDatabase

class TambahPelanggan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etAlamat: EditText
    lateinit var etNoHP: EditText
    lateinit var etCabang: EditText
    lateinit var btSimpan: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pelanggan)
        enableEdgeToEdge()
        init()
        cekValidasi()
        simpan()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvJudul = findViewById(R.id.tv_title)
        etNama = findViewById(R.id.et_nama)
        etAlamat = findViewById(R.id.et_alamat)
        etNoHP = findViewById(R.id.et_hp)
        etCabang = findViewById(R.id.et_cabang)
        btSimpan = findViewById(R.id.btn_simpan)
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHP = etNoHP.text.toString()
        val cabang = etCabang.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasi_nama_pelanggan)
            Toast.makeText(
                this,
                this.getString(R.string.validasi_nama_pelanggan),
                Toast.LENGTH_SHORT
            ).show()
            etNama.requestFocus()
        }

        if (alamat.isEmpty()) {
            etAlamat.error = this.getString(R.string.validasi_nama_pelanggan)
            Toast.makeText(
                this,
                this.getString(R.string.validasi_nama_pelanggan),
                Toast.LENGTH_SHORT
            ).show()
            etNama.requestFocus()
        }

        if (noHP.isEmpty()) {
            etNoHP.error = this.getString(R.string.validasi_nama_pelanggan)
            Toast.makeText(
                this,
                this.getString(R.string.validasi_nama_pelanggan),
                Toast.LENGTH_SHORT
            ).show()
            etNama.requestFocus()
        }

        if (cabang.isEmpty()) {
            etCabang.error = this.getString(R.string.validasi_nama_pelanggan)
            Toast.makeText(
                this,
                this.getString(R.string.validasi_nama_pelanggan),
                Toast.LENGTH_SHORT
            ).show()
            etNama.requestFocus()
        }


    }

    fun simpan() {
        btSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val alamat = etAlamat.text.toString()
            val noHP = etNoHP.text.toString()
            val cabang = etCabang.text.toString()

            if (nama.isEmpty() || alamat.isEmpty() || noHP.isEmpty() || cabang.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pelangganBaru = myRef.push()
            val pelangganId = pelangganBaru.key ?: "Unknown"
            val timestamp = System.currentTimeMillis().toString()
            val data = modelpelanggan(pelangganId, nama, alamat, noHP, cabang, timestamp)

            pelangganBaru.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        this.getString(R.string.sukses_simpan_pelanggan),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        this.getString(R.string.gagal_simpan_pelanggan),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
