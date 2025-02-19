package com.elnoah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modellayanan

class DataLayananAdapter(private val listLayanan: List<modellayanan>) :
    RecyclerView.Adapter<DataLayananAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]

        // Pastikan data tidak null sebelum mengatur teks
        holder.tvIdLayanan.text = item.idLayanan ?: "ID Tidak Tersedia"
        holder.tvNamaLayanan.text = item.namaLayanan ?: "Nama Layanan Tidak Tersedia"
        holder.tvCabangLayanan.text = item.idCabangLayanan ?: "Cabang Tidak Tersedia"

        // Tambahkan aksi klik untuk cardView
        holder.cardViewLayanan.setOnClickListener {
            // Implementasikan aksi klik di sini
        }

        // Tambahkan aksi klik untuk tombol hubungi
        holder.btHubungi.setOnClickListener {
            // Implementasikan aksi hubungi di sini
        }

        // Tambahkan aksi klik untuk tombol lihat layanan
        holder.btLihatLayanan.setOnClickListener {
            // Implementasikan aksi lihat layanan di sini
        }
    }

    override fun getItemCount(): Int {
        return listLayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewLayanan: View = itemView.findViewById(R.id.CVDATA_LAYANAN)
        val tvIdLayanan: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvDataNamaLayanan)
        val tvCabangLayanan: TextView = itemView.findViewById(R.id.tvDataCabangLayanan)
        val btHubungi: Button = itemView.findViewById(R.id.btDataHubungiLayanan)
        val btLihatLayanan: Button = itemView.findViewById(R.id.btDataLihatLayanan)
    }
}