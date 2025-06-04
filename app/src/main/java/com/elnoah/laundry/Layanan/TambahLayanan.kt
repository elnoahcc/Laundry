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
import com.elnoah.laundry.modeldata.modellayanan
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

    private var isEditMode = false
    private var layananId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_layanan)

        // Inisialisasi View
        etNama = findViewById(R.id.etTambahNamaLayanan)
        etHarga = findViewById(R.id.etTambahHargaLayanan)
        etCabang = findViewById(R.id.etTambahCabangLayanan)
        btSimpan = findViewById(R.id.btnSimpanLayanan)

        checkEditMode()

        // Set event listener untuk tombol simpan
        btSimpan.setOnClickListener {
            validasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkEditMode() {
        // Cek apakah ini mode edit dari intent
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        if (isEditMode) {
            // Ubah title dan button text untuk mode edit
            title = "Edit Layanan"
            btSimpan.text = "Update"

            // Ambil data dari intent dan set sebagai placeholder
            layananId = intent.getStringExtra("LAYANAN_ID")

            val namaLayanan = intent.getStringExtra("NAMA_LAYANAN")
            val hargaLayanan = intent.getStringExtra("HARGA_LAYANAN")
            val cabangLayanan = intent.getStringExtra("CABANG_LAYANAN")

            // Set data sebagai placeholder di EditText
            etNama.setText(namaLayanan)
            etHarga.setText(hargaLayanan)
            etCabang.setText(cabangLayanan)

        } else {
            // Mode tambah baru
            title = "Tambah Layanan"
            btSimpan.text = "Simpan"
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

        // Jika semua validasi lolos, cek mode
        if (isEditMode) {
            updateLayanan(nama, harga, cabang)
        } else {
            simpanLayanan(nama, harga, cabang)
        }
    }

    private fun updateLayanan(nama: String, harga: String, cabang: String) {
        layananId?.let { id ->
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val tanggalUpdate = sdf.format(Date())

            val updatedLayanan = mapOf(
                "namaLayanan" to nama,
                "hargaLayanan" to harga,
                "cabangLayanan" to cabang,
                "tanggalUpdate" to tanggalUpdate
            )

            myRef.child(id).updateChildren(updatedLayanan)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal memperbarui layanan: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun simpanLayanan(nama: String, harga: String, cabang: String) {
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
                setResult(RESULT_OK)
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