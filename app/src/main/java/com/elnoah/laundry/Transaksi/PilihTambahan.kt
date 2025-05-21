package com.elnoah.laundry.Transaksi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.PilihTambahanAdapter
import com.elnoah.laundry.modeldata.modeltransaksitambahan
import com.google.firebase.database.*

class PilihTambahan : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listTambahan = mutableListOf<modeltransaksitambahan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_tambahan)

        recyclerView = findViewById(R.id.rvDATA_TAMBAHAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbRef = FirebaseDatabase.getInstance().getReference("tambahan")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listTambahan.clear()
                for (data in snapshot.children) {
                    val tambahan = data.getValue(modeltransaksitambahan::class.java)
                    if (tambahan != null) {
                        listTambahan.add(tambahan)
                    }
                }

                val adapter = PilihTambahanAdapter(listTambahan) { selectedTambahan ->
                    val intent = Intent()
                    intent.putExtra("idLayanan", selectedTambahan.idLayanan)
                    intent.putExtra("namaLayanan", selectedTambahan.namaLayanan)
                    intent.putExtra("hargaLayanan", selectedTambahan.hargaLayanan)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihTambahan, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
