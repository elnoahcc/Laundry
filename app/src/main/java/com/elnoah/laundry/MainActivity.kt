package com.elnoah.laundry

import android.os.Bundle
import android.content.Intent
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elnoah.laundry.Layanan.TambahLayanan
import com.elnoah.laundry.Pegawai.DataPegawai
import com.elnoah.laundry.pelanggan.DataPelanggan

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableEdgeToEdge()

        // Redirect ke halaman pelanggan
        val appDataPelanggan = findViewById<ConstraintLayout>(R.id.clPelanggan)
        appDataPelanggan.setOnClickListener {
            val intent = Intent(this, DataPelanggan::class.java)
            startActivity(intent)
        }

        // Redirect ke halaman pelanggan
        val pegawai = findViewById<CardView>(R.id.cardPegawai)
        pegawai.setOnClickListener {
            val intent = Intent(this, DataPegawai::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
