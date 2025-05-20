package com.elnoah.laundry.Layanan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modellayanan // Pastikan model sesuai dengan struktur Firebase
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahLayanan : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan)

        // Inisialisasi View
        etNama = findViewById(R.id.etTambahNamaLayanan)
        etHarga = findViewById(R.id.etTambahHargaLayanan)
        etCabang = findViewById(R.id.etTambahCabangLayanan)
        btSimpan = findViewById(R.id.btnSimpanLayanan)

        // Set event listener untuk tombol simpan
        btSimpan.setOnClickListener {
            validasi()
        }
    }

    private fun validasi() {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama_layanan)
            etNama.requestFocus()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = getString(R.string.validasi_harga_layanan)
            etHarga.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_cabang_layanan)
            etCabang.requestFocus()
            return
        }

        // Jika semua validasi lolos, simpan data
        simpan(nama, harga, cabang)
    }

    private fun simpan(nama: String, harga: String, cabang: String) {
        val layananBaru = myRef.push()
        val layananId = layananBaru.key ?: return

        // Tambahkan tanggal saat ini
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalTerdaftar = sdf.format(Date())

        val data = modellayanan(
            idLayanan = layananId,
            namaLayanan = nama,
            hargaLayanan = harga,
            cabangLayanan = cabang,
            tanggalTerdaftar = tanggalTerdaftar
        )

        layananBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.sukses_simpan_pelanggan),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.gagal_simpan_pelanggan),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

