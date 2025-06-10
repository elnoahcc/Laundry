package com.elnoah.laundry.Transaksi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.PilihTambahanAdapter
import com.elnoah.laundry.modeldata.modeltransaksitambahan
import com.google.firebase.database.*

class PilihTambahan : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvKosong: TextView
    private lateinit var adapter: PilihTambahanAdapter

    private val listTambahan = mutableListOf<modeltransaksitambahan>()
    private val filteredList = mutableListOf<modeltransaksitambahan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_tambahan)
        // Initialize views
        recyclerView = findViewById(R.id.rvDATA_TAMBAHAN)
        searchView = findViewById(R.id.svCariDataTambahan)
        tvKosong = findViewById(R.id.tvKosongTambahan)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter
        adapter = PilihTambahanAdapter(filteredList) { selectedTambahan ->
            val intent = Intent()
            intent.putExtra("idLayanan", selectedTambahan.idLayanan)
            intent.putExtra("namaLayanan", selectedTambahan.namaLayanan)
            intent.putExtra("hargaLayanan", selectedTambahan.hargaLayanan)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = adapter

        // Setup search functionality
        setupSearchView()

        // Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference("tambahan")

        // Load data from Firebase
        loadDataFromFirebase()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText ?: "")
                return true
            }
        })
    }

    private fun filterData(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            // If search query is empty, show all data
            filteredList.addAll(listTambahan)
        } else {
            // Filter data based on search query
            val searchQuery = query.lowercase()
            for (tambahan in listTambahan) {
                if (tambahan.namaLayanan?.lowercase()?.contains(searchQuery) == true ||
                    tambahan.idLayanan?.lowercase()?.contains(searchQuery) == true ||
                    tambahan.hargaLayanan?.toString()?.contains(searchQuery) == true) {
                    filteredList.add(tambahan)
                }
            }
        }

        // Update UI based on filtered results
        updateUI()

        // Notify adapter about data changes
        adapter.notifyDataSetChanged()
    }

    private fun updateUI() {
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvKosong.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvKosong.visibility = View.GONE
        }
    }

    private fun loadDataFromFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listTambahan.clear()
                for (data in snapshot.children) {
                    val tambahan = data.getValue(modeltransaksitambahan::class.java)
                    if (tambahan != null) {
                        listTambahan.add(tambahan)
                    }
                }

                // Initialize filtered list with all data
                filteredList.clear()
                filteredList.addAll(listTambahan)

                // Update UI
                updateUI()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihTambahan, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}