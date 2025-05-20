package com.elnoah.laundry.Transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.PilihPelangganAdapter
import com.elnoah.laundry.modeldata.modelpelanggan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PilihPelanggan : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listPelanggan = mutableListOf<modelpelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_pelanggan)

        recyclerView = findViewById(R.id.rvDATA_TRANSAKSI_PELANGGAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbRef = FirebaseDatabase.getInstance().getReference("pelanggan")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                for (data in snapshot.children) {
                    val pelanggan = data.getValue(modelpelanggan::class.java)
                    if (pelanggan != null) {
                        listPelanggan.add(pelanggan)
                    }
                }

                val adapter = PilihPelangganAdapter(listPelanggan) { selectedPelanggan ->
                    val intent = Intent()
                    intent.putExtra("idPelanggan", selectedPelanggan.idPelanggan)
                    intent.putExtra("namaPelanggan", selectedPelanggan.namaPelanggan)
                    intent.putExtra("noHPPelanggan", selectedPelanggan.noHPPelanggan)
                    setResult(RESULT_OK, intent)
                    finish()
                }


                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihPelanggan, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
