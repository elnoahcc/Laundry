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
        setContentView(R.layout.activity_data_transaksi)
        init()
        FirebaseApp.initializeApp(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        rvLayananTambahan.layoutManager = layoutManager
        rvLayananTambahan.setHasFixedSize(true)
        btnPilihPelanggan.setOnClickListener {
            val intent = Intent(this, PilihPelanggan::class.java)
            startActivityForResult(intent,pilihPelanggan)
        }
        btnPilihLayanan.setOnClickListener {
            val intent = Intent(this, PilihLayanan::class.java)
            startActivityForResult(intent,pilihLayanan)
        }
        btnTambahan.setOnClickListener {
            val intent = Intent(this, PilihLayanan::class.java)
            startActivityForResult(intent,pilihLayananTambahan)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun init() {
        tvPelangganNama = findViewById(R.id.tvNamaPelanggan)
        tvPelangganNoHP = findViewById(R.id.tvPelangganNoHP)
        tvLayananNama = findViewById(R.id.tvNamaLayanan)
        tvLayananHarga = findViewById(R.id.tvLayananHarga)
        rvLayananTambahan = findViewById(R.id.rvLayananTambahan)
        btnPilihPelanggan = findViewById(R.id.btnPilihPelanggan)
        btnPilihLayanan = findViewById(R.id.btnPilihLayanan)
        btnTambahan = findViewById(R.id.btnTambahan)
    }
    @Deprecated( "This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pilihPelanggan) {
            if (resultCode == RESULT_OK && data != null) {
                idPelanggan = data.getStringExtra("idPelanggan").toString()
                val nama = data.getStringExtra("nama")
                val nomorHP = data.getStringExtra("noHP")

                tvPelangganNama.text = "Nama Pelanggan : $nama"
                tvPelangganNoHP.text = "No HP : $nomorHP"

                namaPelanggan = nama.toString()
                noHP = nomorHP.toString()
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Batal Memilih Pelanggan", Toast.LENGTH_SHORT).show()
            }
        }
    }

}