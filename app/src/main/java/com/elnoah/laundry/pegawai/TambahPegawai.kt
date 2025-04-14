package com.elnoah.laundry.pegawai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelpegawai
import com.google.firebase.database.FirebaseDatabase

class TambahPegawai : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var tvtitleP: TextView
    private lateinit var etNamaP: EditText
    private lateinit var etAlamatP: EditText
    private lateinit var etNoHPP: EditText
    private lateinit var etCabangP: EditText
    private lateinit var btSimpanP: Button

    private var idPegawai: String = ""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)
        enableEdgeToEdge()

        init()
        getData()

        btSimpanP.setOnClickListener {
            if (isEditMode) {
                update()
            } else {
                simpan()
            }
        }
    }

    private fun init() {
        tvtitleP = findViewById(R.id.tv_title_pegawai)
        etNamaP = findViewById(R.id.et_nama_pegawai)
        etAlamatP = findViewById(R.id.et_alamat_pegawai)
        etNoHPP = findViewById(R.id.et_hp_pegawai)
        etCabangP = findViewById(R.id.et_cabang_pegawai)
        btSimpanP = findViewById(R.id.btn_simpan_pegawai)
    }

    private fun getData() {
        // Ambil data dari intent
        val judul = intent.getStringExtra("judul") ?: "Tambah Pegawai"
        idPegawai = intent.getStringExtra("idPegawai") ?: ""
        val nama = intent.getStringExtra("namaPegawai") ?: ""
        val alamat = intent.getStringExtra("alamatPegawai") ?: ""
        val hp = intent.getStringExtra("noHPPegawai") ?: ""
        val cabang = intent.getStringExtra("idCabang") ?: ""

        // Tampilkan judul di atas form
        tvtitleP.text = judul
        etNamaP.setText(nama)
        etAlamatP.setText(alamat)
        etNoHPP.setText(hp)
        etCabangP.setText(cabang)

        // Tentukan mode
        isEditMode = judul == "Edit Pegawai"

        if (isEditMode) {
            mati()
            btSimpanP.text = "Sunting"
        } else {
            hidup()
            etNamaP.requestFocus()
            btSimpanP.text = "Simpan"
        }
    }

    private fun mati() {
        etNamaP.isEnabled = false
        etAlamatP.isEnabled = false
        etNoHPP.isEnabled = false
        etCabangP.isEnabled = false
    }

    private fun hidup() {
        etNamaP.isEnabled = true
        etAlamatP.isEnabled = true
        etNoHPP.isEnabled = true
        etCabangP.isEnabled = true
    }

    private fun cekValidasi(): Boolean {
        return when {
            etNamaP.text.isEmpty() -> {
                etNamaP.error = getString(R.string.validasi_nama_pelanggan)
                etNamaP.requestFocus()
                false
            }
            etAlamatP.text.isEmpty() -> {
                etAlamatP.error = getString(R.string.validasi_alamat_pelanggan)
                etAlamatP.requestFocus()
                false
            }
            etNoHPP.text.isEmpty() -> {
                etNoHPP.error = getString(R.string.validasi_nohp_pelanggan)
                etNoHPP.requestFocus()
                false
            }
            etCabangP.text.isEmpty() -> {
                etCabangP.error = getString(R.string.validasi_cabang_pelanggan)
                etCabangP.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun update() {
        if (!cekValidasi()) return

        val pegawaiRef = database.getReference("pegawai").child(idPegawai)
        val updateData = mapOf(
            "namaPegawai" to etNamaP.text.toString(),
            "alamatPegawai" to etAlamatP.text.toString(),
            "noHPPegawai" to etNoHPP.text.toString(),
            "idCabang" to etCabangP.text.toString()
        )

        pegawaiRef.updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pegawai berhasil diperbaharui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data Pegawai gagal diperbaharui", Toast.LENGTH_SHORT).show()
            }
    }

    private fun simpan() {
        if (!cekValidasi()) return

        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key ?: return
        val timestamp = System.currentTimeMillis().toString()

        val data = modelpegawai(
            pegawaiId,
            etNamaP.text.toString(),
            etAlamatP.text.toString(),
            etNoHPP.text.toString(),
            etCabangP.text.toString(),
            timestamp
        )

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.sukses_simpan_pegawai), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_pegawai), Toast.LENGTH_SHORT).show()
            }
    }
}
