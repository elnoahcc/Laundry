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
        holder.tvID.text = item.idLayanan
        holder.tvNamaLayanan.text = item.namaLayanan
        holder.tvCabangLayanan.text = item.idCabangLayanan
        holder.cvCARDLAYANAN.setOnClickListener {
            // Tambahkan aksi klik jika diperlukan
        }

        holder.btHubungi.setOnClickListener {
            // Tambahkan aksi untuk tombol hubungi
        }

        holder.btLihatLayanan.setOnClickListener {
            // Tambahkan aksi untuk tombol lihat layanan
        }
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCARDLAYANAN: View = itemView.findViewById(R.id.CVDATA_LAYANAN)
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tv_nama_layanan)
        val tvCabangLayanan: TextView = itemView.findViewById(R.id.tv_cabang_layanan)
        val btHubungi: Button = itemView.findViewById(R.id.btDataHubungiLayanan)
        val btLihatLayanan: Button = itemView.findViewById(R.id.btDataLihatLayanan)
    }
    override fun getItemCount(): Int {
    return listLayanan.size}
}

