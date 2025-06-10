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
import com.elnoah.laundry.modeldata.modelpelanggan
import com.google.firebase.database.DatabaseReference


class PilihPelangganAdapter(
    private val list: List<modelpelanggan>,
    private val onItemClick: (modelpelanggan) -> Unit
) : RecyclerView.Adapter<PilihPelangganAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaPelanggan)
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDPelanggan)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPelanggan)

        fun bind(pelanggan: modelpelanggan) {
            tvNama.text = pelanggan.namaPelanggan
            tvID.text = "ID: ${pelanggan.idPelanggan}"
            tvNoHp.text = itemView.context.getString(R.string.no_hp_trans, pelanggan.noHPPelanggan)
            tvAlamat.text = pelanggan.alamatPelanggan

            itemView.setOnClickListener {
                onItemClick(pelanggan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_transaksi_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}
