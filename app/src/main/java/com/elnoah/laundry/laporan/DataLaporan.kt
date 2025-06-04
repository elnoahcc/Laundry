package com.elnoah.laundry.laporan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.Transaksi.InvoiceTransaksi
import com.elnoah.laundry.adapter.DataLaporanAdapter
import com.elnoah.laundry.modeldata.modelinvoice
import com.google.firebase.database.*

class DataLaporan : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataLaporanAdapter
    private val listInvoice = arrayListOf<modelinvoice>()
    private lateinit var database: DatabaseReference

    companion object {
        const val REQUEST_CODE_INVOICE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_laporan)

        recyclerView = findViewById(R.id.rvDATA_LAPORAN)
        adapter = DataLaporanAdapter(listInvoice) { invoice ->
            // Aksi pas item diklik, misalnya buka detail
            val intent = Intent(this, InvoiceTransaksi::class.java)
            intent.putExtra("invoice", invoice)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Ambil data dari Firebase
        database = FirebaseDatabase.getInstance().getReference("invoice")
        loadDataInvoice()
    }

    private fun loadDataInvoice() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listInvoice.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val invoice = data.getValue(modelinvoice::class.java)
                        if (invoice != null) {
                            listInvoice.add(invoice)
                        }
                    }
                    Toast.makeText(this@DataLaporan, "Data ditemukan: ${listInvoice.size}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DataLaporan, "Snapshot kosong", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLaporan, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INVOICE && resultCode == RESULT_OK) {
            val newInvoice = data?.getSerializableExtra("invoice") as? modelinvoice
            if (newInvoice != null) {
                listInvoice.add(newInvoice)
                adapter.notifyItemInserted(listInvoice.size - 1)
                Toast.makeText(this, "Invoice baru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
