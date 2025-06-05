package com.elnoah.laundry.laporan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.Transaksi.InvoiceTransaksi
import com.elnoah.laundry.adapter.DataLaporanAdapter
import com.elnoah.laundry.modeldata.modellaporan
import com.elnoah.laundry.modeldata.StatusLaporan
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataLaporan : AppCompatActivity(), DataLaporanAdapter.OnStatusChangeListener, DataLaporanAdapter.OnDeleteListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataLaporanAdapter
    private val laporanList = ArrayList<modellaporan>()
    private lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_laporan)

        database = FirebaseDatabase.getInstance().getReference("Laporan")

        recyclerView = findViewById(R.id.rvDATA_LAPORAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pass listener ke adapter
        adapter = DataLaporanAdapter(laporanList, this, this)
        recyclerView.adapter = adapter

        loadData()
    }

    private fun loadData() {
        database.orderByChild("tanggal").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                laporanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val laporan = dataSnapshot.getValue(modellaporan::class.java)
                    laporan?.let {
                        laporanList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLaporan, "Data load failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Helper function to convert StatusLaporan enum to String
    private fun getStringFromStatus(status: StatusLaporan): String {
        return when (status) {
            StatusLaporan.SUDAH_DIBAYAR -> "SUDAH_DIBAYAR"
            StatusLaporan.BELUM_DIBAYAR -> "BELUM_DIBAYAR"
            StatusLaporan.SELESAI -> "SELESAI"
        }
    }

    override fun onStatusChanged(position: Int, newStatus: StatusLaporan) {
        if (position < laporanList.size) {
            val laporan = laporanList[position]
            val statusString = getStringFromStatus(newStatus)
            laporan.status = statusString // Assign String value instead of enum

            // Jika status berubah ke SELESAI, set tanggal pengambilan
            if (newStatus == StatusLaporan.SELESAI) {
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                laporan.tanggalPengambilan = currentDateTime

                // Update status dan tanggal pengambilan di Firebase
                val updateMap = mapOf(
                    "status" to statusString, // Use String instead of enum
                    "tanggalPengambilan" to currentDateTime
                )

                database.child(laporan.noTransaksi ?: "").updateChildren(updateMap)
                    .addOnSuccessListener {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(this, "Status berhasil diubah menjadi Selesai", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Update hanya status di Firebase
                database.child(laporan.noTransaksi ?: "").child("status").setValue(statusString) // Use String instead of enum
                    .addOnSuccessListener {
                        adapter.notifyItemChanged(position)
                        val statusMessage = when (newStatus) {
                            StatusLaporan.SUDAH_DIBAYAR -> "Status : Sudah dibayar"
                            StatusLaporan.BELUM_DIBAYAR -> "Status berhasil diubah menjadi Belum Dibayar"
                            StatusLaporan.SELESAI -> "Status : sudah selesai"
                        }
                        Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDeleteItem(position: Int) {
        if (position < laporanList.size) {
            val laporan = laporanList[position]
            database.child(laporan.noTransaksi ?: "").removeValue()
                .addOnSuccessListener {
                    laporanList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Laporan gagal dihapus", Toast.LENGTH_SHORT).show()
                }
        }
    }
}