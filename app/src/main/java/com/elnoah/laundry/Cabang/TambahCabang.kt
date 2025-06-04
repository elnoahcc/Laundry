package com.elnoah.laundry.cabang

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelcabang
import com.google.firebase.database.FirebaseDatabase

class TambahCabang : AppCompatActivity() {

    private lateinit var etNamaCabang: EditText
    private lateinit var etAlamatCabang: EditText
    private lateinit var etTeleponCabang: EditText
    private lateinit var btnSimpan: Button

    private val database = FirebaseDatabase.getInstance()
    private val refCabang = database.getReference("cabang")

    private var idCabang: String? = null  // Untuk update data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_cabang)

        etNamaCabang = findViewById(R.id.etTambahNamaCabang)
        etAlamatCabang = findViewById(R.id.etTambahAlamatCabang)
        etTeleponCabang = findViewById(R.id.etTambahTeleponCabang)
        btnSimpan = findViewById(R.id.btnSimpanCabang)

        // Cek apakah ini mode update
        idCabang = intent.getStringExtra("idCabang")
        if (idCabang != null) {
            etNamaCabang.setText(intent.getStringExtra("namaLokasiCabang"))
            etAlamatCabang.setText(intent.getStringExtra("alamatCabang"))
            etTeleponCabang.setText(intent.getStringExtra("teleponCabang"))
        }

        btnSimpan.setOnClickListener {
            val nama = etNamaCabang.text.toString().trim()
            val alamat = etAlamatCabang.text.toString().trim()
            val telepon = etTeleponCabang.text.toString().trim()

            if (nama.isEmpty() || alamat.isEmpty() || telepon.isEmpty()) {
                Toast.makeText(this, (getString(R.string.isisemuafield)), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gunakan ID dari intent jika update, atau generate dari push().key jika tambah baru
            val id = idCabang ?: refCabang.push().key
            if (id == null) {
                Toast.makeText(this, (getString(R.string.gagalmembuatid)), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            simpanDataCabang(id, nama, alamat, telepon)
        }
    }

    private fun simpanDataCabang(id: String, nama: String, alamat: String, telepon: String) {
        val cabang = modelcabang(
            idCabang = id,
            namaLokasiCabang = nama,
            alamatCabang = alamat,
            teleponCabang = telepon
        )

        refCabang.child(id).setValue(cabang)
            .addOnSuccessListener {
                Toast.makeText(this, (getString(R.string.datacabangberhasildisimpan)), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Data Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
