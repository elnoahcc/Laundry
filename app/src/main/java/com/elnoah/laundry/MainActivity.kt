package com.elnoah.laundry

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.Layanan.DataLayanan
import com.elnoah.laundry.pegawai.DataPegawai
import com.elnoah.laundry.pelanggan.DataPelanggan

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge() // Panggil ini sebelum setContentView()

        setContentView(R.layout.activity_main)

        // Redirect ke halaman pelanggan
        findViewById<ConstraintLayout>(R.id.clPelanggan)?.setOnClickListener {
            startActivity(Intent(this, DataPelanggan::class.java))
        }

        // Redirect ke halaman pegawai
        findViewById<CardView>(R.id.cardPegawai)?.setOnClickListener {
            startActivity(Intent(this, DataPegawai::class.java))
        }

        // Redirect ke halaman layanan
        findViewById<CardView>(R.id.cardLayanan)?.setOnClickListener {
            startActivity(Intent(this, DataLayanan::class.java))
        }

        // Atur padding agar sesuai dengan system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
