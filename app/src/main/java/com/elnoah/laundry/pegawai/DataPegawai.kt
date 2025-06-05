package com.elnoah.laundry.pegawai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.adapter.DataPegawaiAdapter
import com.elnoah.laundry.modeldata.modelpegawai
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataPegawai : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabTambahPegawai: FloatingActionButton
    private lateinit var pegawaiList: ArrayList<modelpegawai>
    private lateinit var adapter: DataPegawaiAdapter
    private var valueEventListener: ValueEventListener? = null

    private fun init() {
        rvDataPegawai = findViewById(R.id.rvDATA_PEGAWAI)
        fabTambahPegawai = findViewById(R.id.fabDATA_PEGAWAI_TAMBAH)

        pegawaiList = ArrayList()
        adapter = DataPegawaiAdapter(
            pegawaiList,
            onItemClick = { pegawai ->
                // Untuk edit pegawai
                val intent = Intent(this@DataPegawai, TambahPegawai::class.java).apply {
                    putExtra("idPegawai", pegawai.idPegawai)
                    putExtra("namaPegawai", pegawai.namaPegawai)
                    putExtra("alamatPegawai", pegawai.alamatPegawai)
                    putExtra("noHPPegawai", pegawai.noHPPegawai)
                    putExtra("cabangPegawai", pegawai.cabangPegawai)
                    putExtra("tanggalTerdaftar", pegawai.tanggalTerdaftar)
                }
                startActivity(intent)
            },
            onDeleteClick = { pegawai ->
                // Untuk delete pegawai dengan konfirmasi
                showDeleteConfirmation(pegawai)
            }
        )

        rvDataPegawai.layoutManager = LinearLayoutManager(this)
        rvDataPegawai.adapter = adapter
    }

    private fun getDATA() {
        valueEventListener?.let { myRef.removeEventListener(it) } // hapus listener lama

        val query = myRef.orderByChild("idPegawai").limitToLast(100)
        valueEventListener = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pegawaiList.clear()
                for (dataSnapshot in snapshot.children) {
                    val pegawai = dataSnapshot.getValue(modelpegawai::class.java)
                    pegawai?.let { pegawaiList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPegawai, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("DataPegawai", "Firebase Error: ${error.toException()}")
            }
        })
    }

    // Fungsi untuk menampilkan konfirmasi delete
    private fun showDeleteConfirmation(pegawai: modelpegawai) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Hapus")
        builder.setMessage("Apakah Anda yakin ingin menghapus data pegawai ${pegawai.namaPegawai ?: "ini"}?")

        builder.setPositiveButton("Ya") { _, _ ->
            deletePegawai(pegawai)
        }

        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    // Fungsi untuk menghapus pegawai
    fun deletePegawai(pegawai: modelpegawai) {
        val idPegawai = pegawai.idPegawai
        if (!idPegawai.isNullOrEmpty()) {
            val pegawaiRef = myRef.child(idPegawai)
            pegawaiRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Data pegawai berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal menghapus data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DataPegawai", "Delete Error: ${exception}")
                }
        } else {
            Toast.makeText(this, "ID Pegawai tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)

        init()
        getDATA()

        fabTambahPegawai.setOnClickListener {
            val intent = Intent(this, TambahPegawai::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        getDATA() // Refresh data setiap balik ke activity ini
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let { myRef.removeEventListener(it) }
    }
}