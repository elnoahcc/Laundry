package com.elnoah.laundry.Layanan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.DataLayananAdapter
import com.elnoah.laundry.modeldata.modellayanan
import com.elnoah.laundry.Layanan.TambahLayanan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataLayanan : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")
    private lateinit var rvDATA_LAYANAN: RecyclerView
    private lateinit var fabDATA_LAYANAN_TAMBAH: FloatingActionButton
    private lateinit var layananList: ArrayList<modellayanan>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_layanan)

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDATA_LAYANAN.layoutManager = layoutManager
        rvDATA_LAYANAN.setHasFixedSize(true)

        layananList = arrayListOf()
        getData()

        fabDATA_LAYANAN_TAMBAH.setOnClickListener {
            val intent = Intent(this, TambahLayanan::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        rvDATA_LAYANAN = findViewById(R.id.rvDATA_LAYANAN)
        fabDATA_LAYANAN_TAMBAH = findViewById(R.id.fabDATA_LAYANAN_TAMBAH)
    }

    private fun getData() {
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    layananList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(modellayanan::class.java)
                        layanan?.let { layananList.add(it) }
                    }
                    val adapter = DataLayananAdapter(layananList)
                    rvDATA_LAYANAN.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLayanan, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
