package com.elnoah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modeltransaksitambahan

class LayananTambahanAdapter(
    private val list: List<modeltransaksitambahan>
) : RecyclerView.Adapter<LayananTambahanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.tv_nama_tambahan)
        val harga: TextView = itemView.findViewById(R.id.tv_harga_tambahan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_data_tambahan, parent, false) // Harus item layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.nama.text = item.namaLayanan
        holder.harga.text = "Rp. ${item.hargaLayanan}"
    }

    override fun getItemCount(): Int = list.size
}
