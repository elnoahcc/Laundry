package com.elnoah.laundry.Layanan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDialog
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
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class DataLayanan : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    private lateinit var rvDataLayanan: RecyclerView
    private lateinit var fabTambahLayanan: FloatingActionButton
    private lateinit var layananList: ArrayList<modellayanan>
    private lateinit var adapter: DataLayananAdapter

    private fun init() {
        rvDataLayanan = findViewById(R.id.rvDATA_LAYANAN)
        fabTambahLayanan = findViewById(R.id.fabTambahLayanan)

        rvDataLayanan.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAdapter() {
        adapter = DataLayananAdapter(
            layananList,
            onEditClick = { layanan, position ->
                // Handle edit/sunting - Intent ke activity edit
                val intent = Intent(this, TambahLayanan::class.java) // atau EditLayanan::class.java
                intent.putExtra("EDIT_MODE", true)
                intent.putExtra("LAYANAN_ID", layanan.idLayanan)
                intent.putExtra("NAMA_LAYANAN", layanan.namaLayanan)
                intent.putExtra("HARGA_LAYANAN", layanan.hargaLayanan)
                intent.putExtra("CABANG_LAYANAN", layanan.cabangLayanan)
                intent.putExtra("POSITION", position)
                startActivityForResult(intent, 100) // Request code 100 untuk edit
            },
            onDeleteClick = { layanan, position ->
                // Handle delete dari Firebase
                deleteLayananFromFirebase(layanan, position)
            },
            onViewClick = { layanan, position ->
                // Handle view - Tampilkan dialog_mod_layanan
                showViewLayananDialog(layanan)
            }
        )
        rvDataLayanan.adapter = adapter
    }

    private fun deleteLayananFromFirebase(layanan: modellayanan, position: Int) {
        layanan.idLayanan?.let { id ->
            myRef.child(id).removeValue()
                .addOnSuccessListener {
                    if (position >= 0) {
                        adapter.removeItem(position)
                    } else {
                        // Refresh data jika dipanggil dari dialog
                        getDATA()
                    }
                    Toast.makeText(this, "Layanan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal menghapus layanan: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showViewLayananDialog(layanan: modellayanan) {
        val dialog = AppCompatDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mod_layanan, null)

        // Bind data ke dialog
        val tvDialogIdLayanan = dialogView.findViewById<TextView>(R.id.tvDialogIdLayanan)
        val tvDialogNamaLayanan = dialogView.findViewById<TextView>(R.id.tvDialogNamaLayanan)
        val tvDialogHargaLayanan = dialogView.findViewById<TextView>(R.id.tvDialogHargaLayanan)
        val tvDialogCabangLayanan = dialogView.findViewById<TextView>(R.id.tvDialogCabangLayanan)
        val tvDialogTanggalTerdaftar = dialogView.findViewById<TextView>(R.id.tvDialogTanggalTerdaftar)
        val btnEditLayanan = dialogView.findViewById<Button>(R.id.btn_edit_layanan)
        val btnDeleteLayanan = dialogView.findViewById<Button>(R.id.btn_delete_layanan)

        tvDialogIdLayanan?.text = layanan.idLayanan ?: "-"
        tvDialogNamaLayanan?.text = layanan.namaLayanan ?: "-"
        tvDialogHargaLayanan?.text = layanan.hargaLayanan ?: "-"
        tvDialogCabangLayanan?.text = layanan.cabangLayanan ?: "-"
        tvDialogTanggalTerdaftar?.text = layanan.tanggalTerdaftar ?: "-"

        // Handle tombol Sunting
        btnEditLayanan?.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TambahLayanan::class.java)
            intent.putExtra("EDIT_MODE", true)
            intent.putExtra("LAYANAN_ID", layanan.idLayanan)
            intent.putExtra("NAMA_LAYANAN", layanan.namaLayanan)
            intent.putExtra("HARGA_LAYANAN", layanan.hargaLayanan)
            intent.putExtra("CABANG_LAYANAN", layanan.cabangLayanan)
            startActivityForResult(intent, 100)
        }

        // Handle tombol Hapus
        btnDeleteLayanan?.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(layanan)
        }

        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showDeleteConfirmationDialog(layanan: modellayanan) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus layanan \"${layanan.namaLayanan}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteLayananFromFirebase(layanan, -1)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun getDATA() {
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    layananList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(modellayanan::class.java)
                        layanan?.let { layananList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLayanan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_layanan)

        layananList = ArrayList()
        init()
        setupAdapter()
        getDATA()

        fabTambahLayanan.setOnClickListener {
            val intent = Intent(this, TambahLayanan::class.java)
            startActivityForResult(intent, 200) // Request code 200 untuk tambah
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> {
                    // Edit result - refresh data
                    getDATA()
                    Toast.makeText(this, "Data layanan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                200 -> {
                    // Add result - refresh data
                    getDATA()
                    Toast.makeText(this, "Layanan baru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}