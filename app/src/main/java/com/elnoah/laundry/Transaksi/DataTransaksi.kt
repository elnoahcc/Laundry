package com.elnoah.laundry.Transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modeltransaksitambahan
import com.google.firebase.FirebaseApp

class DataTransaksi : AppCompatActivity() {
    private lateinit var tvPelangganNama: TextView
    private lateinit var tvPelangganNoHP: TextView
    private lateinit var tvLayananNama: TextView
    private lateinit var tvLayananHarga: TextView
    private lateinit var rvLayananTambahan: RecyclerView
    private lateinit var btnPilihPelanggan: Button
    private lateinit var btnPilihLayanan: Button
    private lateinit var btnTambahan: Button
    private lateinit var btn_proses: Button
    private val dataList = mutableListOf<modeltransaksitambahan>()

    private val pilihPelanggan = 1
    private val pilihLayanan = 2
    private val pilihLayananTambahan = 3

    private var idPelanggan: String = ""
    private var idCabang: String = ""
    private var namaPelanggan: String = ""
    private var noHP: String = ""
    private var idLayanan: String = ""
    private var namaLayanan: String = ""
    private var hargaLayanan: String = ""
    private var idPegawai: String = ""

    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_transaksi)

        sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        idCabang = sharedPref.getString("idCabang", null).toString()
        idPegawai = sharedPref.getString("idPegawai", null).toString()

        init()
        FirebaseApp.initializeApp(this)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        rvLayananTambahan.layoutManager = layoutManager
        rvLayananTambahan.setHasFixedSize(true)

        btnPilihPelanggan.setOnClickListener {
            val intent = Intent(this, PilihPelanggan::class.java)
            startActivityForResult(intent, pilihPelanggan)
        }

        btnPilihLayanan.setOnClickListener {
            val intent = Intent(this, PilihLayanan::class.java)
            startActivityForResult(intent, pilihLayanan)
        }

        btnTambahan.setOnClickListener {
            val intent = Intent(this, PilihTambahan::class.java)
            startActivityForResult(intent, pilihLayananTambahan)
        }

        btn_proses.setOnClickListener {
            // Validasi sebelum melanjutkan ke KonfirmasiTransaksi
            if (validateData()) {
                val intent = Intent(this, KonfirmasiTransaksi::class.java)
                intent.putExtra("namaPelanggan", namaPelanggan)
                intent.putExtra("noHP", noHP)
                intent.putExtra("namaLayanan", namaLayanan)
                intent.putExtra("hargaLayanan", hargaLayanan)
                intent.putExtra("listTambahan", ArrayList(dataList))
                startActivity(intent)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        tvPelangganNama = findViewById(R.id.tv_nama_pelanggan)
        tvPelangganNoHP = findViewById(R.id.tv_no_hp)
        tvLayananNama = findViewById(R.id.tv_nama_layanan)
        tvLayananHarga = findViewById(R.id.tv_harga_layanan)
        rvLayananTambahan = findViewById(R.id.rv_layanan_tambahan)
        btnPilihPelanggan = findViewById(R.id.btn_pilih_pelanggan)
        btnPilihLayanan = findViewById(R.id.btn_pilih_layanan)
        btnTambahan = findViewById(R.id.btn_tambahan)
        btn_proses = findViewById(R.id.btn_proses)
    }

    // Fungsi validasi untuk memastikan pelanggan dan layanan sudah dipilih
    private fun validateData(): Boolean {
        val isPelangganSelected = namaPelanggan.isNotEmpty() && namaPelanggan != "null"
        val isLayananSelected = namaLayanan.isNotEmpty() && namaLayanan != "null"

        return when {
            !isPelangganSelected && !isLayananSelected -> {
                Toast.makeText(this, "Pilih Pelanggan dan Layanan terlebih dahulu", Toast.LENGTH_SHORT).show()
                false
            }
            !isPelangganSelected -> {
                Toast.makeText(this, "Pilih Pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                false
            }
            !isLayananSelected -> {
                Toast.makeText(this, "Pilih Layanan terlebih dahulu", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    // Fungsi untuk mengupdate RecyclerView dengan callback hapus item
    private fun updateRecyclerView() {
        rvLayananTambahan.adapter = com.elnoah.laundry.adapter.PilihTambahanAdapter(dataList) { selectedTambahan ->
            // Hapus item dari dataList
            dataList.remove(selectedTambahan)
            // Update RecyclerView lagi setelah item dihapus
            updateRecyclerView()
            // Tampilkan pesan konfirmasi
            Toast.makeText(this, "Layanan tambahan ${selectedTambahan.namaLayanan} dihapus", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            pilihPelanggan -> {
                if (resultCode == RESULT_OK && data != null) {
                    idPelanggan = data.getStringExtra("idPelanggan").toString()
                    val nama = data.getStringExtra("namaPelanggan")
                    val nomorHP = data.getStringExtra("noHPPelanggan")

                    tvPelangganNama.text = getString(R.string.nama_pelanggan_trans, nama)
                    tvPelangganNoHP.text = "No HP : $nomorHP"

                    namaPelanggan = nama.toString()
                    noHP = nomorHP.toString()
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Batal Memilih Pelanggan", Toast.LENGTH_SHORT).show()
                }
            }

            pilihLayanan -> {
                if (resultCode == RESULT_OK && data != null) {
                    idLayanan = data.getStringExtra("idLayanan").toString()
                    val nama = data.getStringExtra("namaLayanan")
                    val harga = data.getStringExtra("hargaLayanan")

                    tvLayananNama.text = "Layanan : $nama"
                    tvLayananHarga.text = "Harga : Rp. $harga"

                    namaLayanan = nama.toString()
                    hargaLayanan = harga.toString()
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Batal Memilih Layanan", Toast.LENGTH_SHORT).show()
                }
            }

            pilihLayananTambahan -> {
                if (resultCode == RESULT_OK && data != null) {
                    val id = data.getStringExtra("idLayanan").toString()
                    val nama = data.getStringExtra("namaLayanan").toString()
                    val harga = data.getStringExtra("hargaLayanan").toString()

                    // Fixed: Use constructor directly instead of apply block
                    // Since properties are val (immutable), create object with constructor parameters
                    val tambahan = modeltransaksitambahan(
                        idLayanan = id,
                        namaLayanan = nama,
                        hargaLayanan = harga,
                        tanggalTerdaftar = "" // You can set current date here if needed
                    )

                    dataList.add(tambahan)
                    updateRecyclerView()
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Batal Memilih Tambahan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}