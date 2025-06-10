package com.elnoah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modeltransaksitambahan

class PilihTambahanAdapter(
    private val list: List<modeltransaksitambahan>,
    private val onItemClick: (modeltransaksitambahan) -> Unit
) : RecyclerView.Adapter<PilihTambahanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tv_nama_tambahan)
        val tvID: TextView = itemView.findViewById(R.id.tv_id_tambahan)
        val tvHarga: TextView = itemView.findViewById(R.id.tv_harga_tambahan)

        fun bind(tambahan: modeltransaksitambahan) {
            tvNama.text = tambahan.namaLayanan
            tvID.text = "ID: ${tambahan.idLayanan}"
            tvHarga.text = "Rp. ${tambahan.hargaLayanan}"

            // Regular click untuk memilih tambahan (bukan long press)
            itemView.setOnClickListener {
                onItemClick(tambahan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}