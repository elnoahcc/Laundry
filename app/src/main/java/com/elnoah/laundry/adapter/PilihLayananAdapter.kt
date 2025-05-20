package com.elnoah.laundry.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.Transaksi.DataTransaksi
import com.elnoah.laundry.modeldata.modellayanan
import com.google.firebase.database.DatabaseReference

class PilihLayananAdapter(
    private val list: List<modellayanan>,
    private val onItemClick: (modellayanan) -> Unit
) : RecyclerView.Adapter<PilihLayananAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaLayanan)
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDataHargaLayanan)

        fun bind(layanan: modellayanan) {
            tvNama.text = layanan.namaLayanan
            tvID.text = "ID: ${layanan.idLayanan}"
            tvHarga.text = "Harga: ${layanan.hargaLayanan}"

            itemView.setOnClickListener {
                onItemClick(layanan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_transaksi_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}