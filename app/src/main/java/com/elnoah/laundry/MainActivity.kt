package com.elnoah.laundry

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.Layanan.DataLayanan // <-- pastikan import ini ada
import com.elnoah.laundry.Transaksi.DataTransaksi
import com.elnoah.laundry.laporan.DataLaporan
import com.elnoah.laundry.Tambahan.DataTambahan
import com.elnoah.laundry.pegawai.DataPegawai
import com.elnoah.laundry.pelanggan.DataPelanggan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge() // Panggil ini sebelum setContentView()


        setContentView(R.layout.activity_main)
        val dateTextView: TextView = findViewById(R.id.date)
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate
        // Redirect ke halaman pelanggan
        findViewById<ConstraintLayout>(R.id.clPelanggan)?.setOnClickListener {
            startActivity(Intent(this, DataPelanggan::class.java))
        }

        findViewById<ConstraintLayout>(R.id.CLtambahan)?.setOnClickListener{
            startActivity(Intent(this, DataTambahan::class.java))
        }

        findViewById<ConstraintLayout>(R.id.clTransaksi)?.setOnClickListener {
            startActivity(Intent(this, DataTransaksi::class.java))
        }

        findViewById<ConstraintLayout>(R.id.clLaporan)?.setOnClickListener {
            startActivity(Intent(this, DataLaporan::class.java))
        }

        // Redirect ke halaman pegawai
        findViewById<CardView>(R.id.cardPegawai)?.setOnClickListener {
            startActivity(Intent(this, DataPegawai::class.java))
        }

        // Redirect ke halaman layanan
        findViewById<CardView>(R.id.cardLayanan)?.setOnClickListener {
            startActivity(Intent(this, DataLayanan::class.java))
        }

        // Redirect ke halaman layanans
        findViewById<ConstraintLayout>(R.id.clLaporan)?.setOnClickListener {
            startActivity(Intent(this, DataLaporan::class.java))
        }

        // Atur padding agar sesuai dengan system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
