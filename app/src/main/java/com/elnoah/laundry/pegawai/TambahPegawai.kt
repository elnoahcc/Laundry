package com.elnoah.laundry.pegawai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelpegawai
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahPegawai : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pegawai)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNama = findViewById(R.id.etTambahNamaPegawai)
        etAlamat = findViewById(R.id.etTambahAlamatPegawai)
        etNoHP = findViewById(R.id.etTambahNoHpPegawai)
        etCabang = findViewById(R.id.etTambahCabangPegawai)
        btSimpan = findViewById(R.id.btnSimpanPegawai)

        val idPegawai = intent.getStringExtra("idPegawai")
        val namaPegawai = intent.getStringExtra("namaPegawai")
        val alamatPegawai = intent.getStringExtra("alamatPegawai")
        val noHPPegawai = intent.getStringExtra("noHPPegawai")
        val cabangPegawai = intent.getStringExtra("cabangPegawai")

        if (idPegawai != null) {
            etNama.setText(namaPegawai)
            etAlamat.setText(alamatPegawai)
            etNoHP.setText(noHPPegawai)
            etCabang.setText(cabangPegawai)
        }

        btSimpan.setOnClickListener {
            validasi()
        }
    }

    private fun validasi() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama_pelanggan)
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = getString(R.string.validasi_alamat_pelanggan)
            etAlamat.requestFocus()
            return
        }
        if (noHP.isEmpty()) {
            etNoHP.error = getString(R.string.validasi_nohp_pelanggan)
            etNoHP.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_cabang_pelanggan)
            etCabang.requestFocus()
            return
        }
        if (!noHP.matches(Regex("\\d{10,13}"))) {
            etNoHP.error = "Nomor HP harus terdiri dari 10-13 angka"
            etNoHP.requestFocus()
            return
        }

        simpan(nama, alamat, noHP, cabang)
    }

    private fun simpan(nama: String, alamat: String, noHP: String, cabang: String) {
        val idPegawai = intent.getStringExtra("idPegawai")
        val isEdit = idPegawai != null

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())

        if (isEdit) {
            val dataUpdate = mapOf<String, Any>(
                "namaPegawai" to nama,
                "alamatPegawai" to alamat,
                "noHPPegawai" to noHP,
                "cabangPegawai" to cabang,
                "tanggalTerdaftar" to tanggalSekarang
            )

            myRef.child(idPegawai!!)
                .updateChildren(dataUpdate)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal update data pegawai", Toast.LENGTH_SHORT).show()
                }

        } else {
            val pegawaiBaru = myRef.push()
            val pegawaiId = pegawaiBaru.key ?: return

            val dataBaru = modelpegawai(
                idPegawai = pegawaiId,
                namaPegawai = nama,
                alamatPegawai = alamat,
                noHPPegawai = noHP,
                cabangPegawai = cabang,
                tanggalTerdaftar = tanggalSekarang
            )

            pegawaiBaru.setValue(dataBaru)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data pegawai berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan data pegawai", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
