package com.elnoah.laundry.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modellayanan

class DataLayananAdapter(
    private val listLayanan: ArrayList<modellayanan>,
    private val onEditClick: ((modellayanan, Int) -> Unit)? = null,
    private val onDeleteClick: ((modellayanan, Int) -> Unit)? = null,
    private val onViewClick: ((modellayanan, Int) -> Unit)? = null
) : RecyclerView.Adapter<DataLayananAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]
        holder.tvDataIDLayanan.text = item.idLayanan ?: ""
        holder.tvNama.text = item.namaLayanan ?: ""
        holder.tvHarga.text = "Rp. ${item.hargaLayanan}"
        holder.tvTerdaftar.text = item.tanggalTerdaftar ?: ""
        holder.tvCabang.text = item.cabangLayanan ?: ""

        // Click listener untuk card view (edit/sunting)
        holder.itemView.setOnClickListener {
            onEditClick?.invoke(item, position)
        }

        // Click listener untuk tombol hapus
        holder.btnHapus?.setOnClickListener {
            showDeleteConfirmation(holder.itemView, item, position)
        }

        // Click listener untuk tombol lihat (dialog_mod_layanan)
        holder.btnLihat?.setOnClickListener {
            onViewClick?.invoke(item, position)
        }
    }

    private fun showDeleteConfirmation(view: View, item: modellayanan, position: Int) {
        AlertDialog.Builder(view.context)
            .setTitle(view.context.getString(R.string.Hapus))
            .setMessage(view.context.getString(R.string.konfirmasi_hapus, item.namaLayanan))
            .setPositiveButton(view.context.getString(R.string.layanan_dihapus)) { _, _ ->
                onDeleteClick?.invoke(item, position)
            }
            .setNegativeButton(view.context.getString(R.string.Hapus),null)
            .show()
    }

    // Fungsi untuk menghapus item dari list
    fun removeItem(position: Int) {
        if (position >= 0 && position < listLayanan.size) {
            listLayanan.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listLayanan.size)
        }
    }

    // Fungsi untuk update item setelah edit
    fun updateItem(position: Int, updatedItem: modellayanan) {
        if (position >= 0 && position < listLayanan.size) {
            listLayanan[position] = updatedItem
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return listLayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataIDLayanan: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaLayanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDataHargaLayanan)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangLayanan)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tv_Terdaftar)

        // Tombol hapus dan lihat
        val btnHapus: Button? = itemView.findViewById(R.id.btnHapus)
        val btnLihat: Button? = itemView.findViewById(R.id.btnLihat)
    }
}