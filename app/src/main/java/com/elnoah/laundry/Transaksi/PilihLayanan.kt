package com.elnoah.laundry.Transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.PilihLayananAdapter
import com.elnoah.laundry.adapter.PilihPelangganAdapter
import com.elnoah.laundry.modeldata.modellayanan
import com.elnoah.laundry.modeldata.modelpelanggan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PilihLayanan: AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listLayanan = mutableListOf<modellayanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_layanan)

        recyclerView = findViewById(R.id.rvDATA_TRANSAKSI_LAYANAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbRef = FirebaseDatabase.getInstance().getReference("layanan")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLayanan.clear()
                for (data in snapshot.children) {
                    val layanan = data.getValue(modellayanan::class.java)
                    if (layanan != null) {
                        listLayanan.add(layanan)
                    }
                }

                val adapter = PilihLayananAdapter(listLayanan) { selectedLayanan ->
                    val intent = Intent()
                    intent.putExtra("idLayanan", selectedLayanan.idLayanan)
                    intent.putExtra("namaLayanan", selectedLayanan.namaLayanan)
                    intent.putExtra("hargaLayanan", selectedLayanan.hargaLayanan)
                    setResult(RESULT_OK, intent)
                    finish()
                }


                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihLayanan, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}