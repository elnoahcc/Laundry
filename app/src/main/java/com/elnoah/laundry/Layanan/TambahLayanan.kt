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

class TambahLayanan : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")
    private lateinit var etNamaLayanan: EditText
    private lateinit var etCabangLayanan: EditText
    private lateinit var btSimpanLayanan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan) // Pastikan layout benar
        init()
        enableEdgeToEdge()
        applyEdgeToEdge()
        btSimpanLayanan.setOnClickListener {simpanLayanan()}
    }

    fun init() {
        etNamaLayanan = findViewById(R.id.et_nama_layanan)
        etCabangLayanan = findViewById(R.id.et_cabang_layanan)
        btSimpanLayanan = findViewById(R.id.btn_simpan_layanan)
    }

     private fun applyEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

     fun cekValidasi(): Boolean {
        if (etNamaLayanan.text.isEmpty()) {
            etNamaLayanan.error = getString(R.string.validasi_nama_layanan)
            etNamaLayanan.requestFocus()
            return false
        }
        if (etCabangLayanan.text.isEmpty()) {
            etCabangLayanan.error = getString(R.string.validasi_cabang_layanan)
            etCabangLayanan.requestFocus()
            return false
        }
        return true
    }

     fun simpanLayanan() {
        try {
            if (!cekValidasi()) return

            val layananBaru = myRef.push()
            val layananId = layananBaru.key

            if (layananId == null) {
                Toast.makeText(this, "Gagal mendapatkan ID layanan!", Toast.LENGTH_SHORT).show()
                return
            }

            // Membuat objek data model untuk disimpan ke Firebase
            val data = modellayanan(
                layananId,
                etNamaLayanan.text.toString(),
                etCabangLayanan.text.toString()
            )

            // Menyimpan data ke Firebase
            layananBaru.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        getString(R.string.sukses_simpan_layanan),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        getString(R.string.gagal_simpan_layanan) + ": " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
