package com.elnoah.laundry.pelanggan

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.DataPelangganAdapter
import com.elnoah.laundry.modeldata.modelpelanggan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataPelanggan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")
    lateinit var rvDATA_PELANGGAN: RecyclerView
    lateinit var fabDATA_PELANGGAN_TAMBAH: FloatingActionButton
    lateinit var pelangganList: ArrayList<modelpelanggan>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pelanggan)
        init()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDATA_PELANGGAN.layoutManager=layoutManager
        rvDATA_PELANGGAN.setHasFixedSize(true)
        pelangganList = arrayListOf<modelpelanggan>()
        getData()
        val intent = Intent(this, TambahPelanggan::class.java)
        intent.putExtra("judul", "")
        intent.putExtra("idPelanggan", "")
        intent.putExtra("namaPelanggan", "")
        intent.putExtra("idPelenggan", "")
        enableEdgeToEdge()

        // Redirect ke halaman pelanggan
        val tambahPelanggan = findViewById<FloatingActionButton>(R.id.fabDATA_PELANGGAN_TAMBAH)
        tambahPelanggan.setOnClickListener {
            val intent = Intent(this, TambahPelanggan::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun init(){
        rvDATA_PELANGGAN = findViewById(R.id.rvDATA_PELANGGAN)
        fabDATA_PELANGGAN_TAMBAH = findViewById(R.id.fabDATA_PELANGGAN_TAMBAH)
    }
    fun getData() {
        val query = myRef.orderByChild("idPelanggan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.exists()){
                   pelangganList.clear()
                   for (dataSnapshot in snapshot.children){
                       val pegawai = dataSnapshot.getValue(modelpelanggan::class.java)
                       pelangganList.add(pegawai!!)
                   }
                   val adapter = DataPelangganAdapter(pelangganList)
                   rvDATA_PELANGGAN.adapter = adapter
                   adapter.notifyDataSetChanged()
               }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPelanggan,error.message,Toast.LENGTH_SHORT)
            }
        })
    }
}