package com.elnoah.laundry.Transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.PilihLayananAdapter
import com.elnoah.laundry.modeldata.modellayanan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PilihLayanan: AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvKosong: TextView
    private lateinit var adapter: PilihLayananAdapter

    private val listLayanan = mutableListOf<modellayanan>()
    private val filteredList = mutableListOf<modellayanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_layanan)

        // Initialize views
        recyclerView = findViewById(R.id.rvDATA_TRANSAKSI_LAYANAN)
        searchView = findViewById(R.id.svCariDataLayanan)
        tvKosong = findViewById(R.id.tvKosongLayanan)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter
        adapter = PilihLayananAdapter(filteredList) { selectedLayanan ->
            val intent = Intent()
            intent.putExtra("idLayanan", selectedLayanan.idLayanan)
            intent.putExtra("namaLayanan", selectedLayanan.namaLayanan)
            intent.putExtra("hargaLayanan", selectedLayanan.hargaLayanan)
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = adapter

        // Setup search functionality
        setupSearchView()

        // Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference("layanan")

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
            filteredList.addAll(listLayanan)
        } else {
            // Filter data based on search query
            val searchQuery = query.lowercase()
            for (layanan in listLayanan) {
                if (layanan.namaLayanan?.lowercase()?.contains(searchQuery) == true ||
                    layanan.idLayanan?.lowercase()?.contains(searchQuery) == true ||
                    layanan.hargaLayanan?.toString()?.contains(searchQuery) == true) {
                    filteredList.add(layanan)
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
                listLayanan.clear()
                for (data in snapshot.children) {
                    val layanan = data.getValue(modellayanan::class.java)
                    if (layanan != null) {
                        listLayanan.add(layanan)
                    }
                }

                // Initialize filtered list with all data
                filteredList.clear()
                filteredList.addAll(listLayanan)

                // Update UI
                updateUI()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihLayanan, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}