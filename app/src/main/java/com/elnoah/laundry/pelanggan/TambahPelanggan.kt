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

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")

    private lateinit var tvJudul: TextView
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    private var idPelanggan: String = ""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pelanggan)
        enableEdgeToEdge()

        init()
        getData()

        btSimpan.setOnClickListener {
            if (!cekValidasi()) return@setOnClickListener

            if (isEditMode) {
                update()
            } else {
                simpan()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        tvJudul = findViewById(R.id.tv_title)
        etNama = findViewById(R.id.et_nama)
        etAlamat = findViewById(R.id.et_alamat)
        etNoHP = findViewById(R.id.et_hp)
        etCabang = findViewById(R.id.et_cabang)
        btSimpan = findViewById(R.id.btn_simpan)
    }

    private fun getData() {
        val judul = intent.getStringExtra("judul") ?: "Tambah Pelanggan"
        idPelanggan = intent.getStringExtra("idPelanggan") ?: ""
        val nama = intent.getStringExtra("namaPelanggan") ?: ""
        val alamat = intent.getStringExtra("alamatPelanggan") ?: ""
        val hp = intent.getStringExtra("noHPPelanggan") ?: ""
        val cabang = intent.getStringExtra("idCabang") ?: ""

        tvJudul.text = judul
        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(hp)
        etCabang.setText(cabang)

        isEditMode = judul == "Edit Pelanggan"
        btSimpan.text = if (isEditMode) "Sunting" else "Simpan"
    }

    private fun cekValidasi(): Boolean {
        when {
            etNama.text.isEmpty() -> {
                etNama.error = getString(R.string.validasi_nama_pelanggan)
                etNama.requestFocus()
                return false
            }
            etAlamat.text.isEmpty() -> {
                etAlamat.error = getString(R.string.validasi_alamat_pelanggan)
                etAlamat.requestFocus()
                return false
            }
            etNoHP.text.isEmpty() -> {
                etNoHP.error = getString(R.string.validasi_nohp_pelanggan)
                etNoHP.requestFocus()
                return false
            }
            etCabang.text.isEmpty() -> {
                etCabang.error = getString(R.string.validasi_cabang_pelanggan)
                etCabang.requestFocus()
                return false
            }
        }
        return true
    }

    private fun simpan() {
        val pelangganBaru = myRef.push()
        val pelangganId = pelangganBaru.key ?: return
        val timestamp = System.currentTimeMillis().toString()

        val data = modelpelanggan(
            pelangganId,
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString(),
            timestamp
        )

        pelangganBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.sukses_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_pelanggan), Toast.LENGTH_SHORT).show()
            }
    }

    private fun update() {
        val pelangganRef = database.getReference("pelanggan").child(idPelanggan)

        val updateData = mapOf(
            "namaPelanggan" to etNama.text.toString(),
            "alamatPelanggan" to etAlamat.text.toString(),
            "noHPPelanggan" to etNoHP.text.toString(),
            "idCabang" to etCabang.text.toString()
        )

        pelangganRef.updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pelanggan berhasil diperbaharui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data pelanggan gagal diperbaharui", Toast.LENGTH_SHORT).show()
            }
    }
}
