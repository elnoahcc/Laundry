package com.elnoah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelinvoice
import java.text.SimpleDateFormat
import java.util.*

class DataLaporanAdapter(
    private val listData: List<modelinvoice>,
    private val onItemClick: (modelinvoice) -> Unit
) : RecyclerView.Adapter<DataLaporanAdapter.DataLaporanViewHolder>() {

    inner class DataLaporanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        val tvTambahan: TextView = itemView.findViewById(R.id.tvTambahan)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val btnAksi: Button = itemView.findViewById(R.id.btnAksi)
        val tvDiambil: TextView = itemView.findViewById(R.id.tvDiambil)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataLaporanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_laporan, parent, false)
        return DataLaporanViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataLaporanViewHolder, position: Int) {
        val item = listData[position]

        holder.tvNama.text = item.namaPelanggan ?: "-"
        holder.tvStatus.text = item.status ?: "-"

        val bgRes = when (item.status?.lowercase(Locale.getDefault())) {
            "selesai" -> R.drawable.bg_status_green
            "proses" -> R.drawable.bg_status_yellow
            "menunggu" -> R.drawable.bg_status_red
            else -> R.drawable.bg_status_gray
        }
        holder.tvStatus.setBackgroundResource(bgRes)

        holder.tvTanggal.text = item.tanggalTerdaftar?.let { dateFormat.format(Date(it)) } ?: "-"
        holder.imgIcon.setImageResource(R.drawable.laundrymachine)

        holder.tvLayanan.text = item.namaLayanan ?: "-"
        holder.tvTambahan.text = "Tambahan: ${item.jumlahTambahan ?: 0}"
        holder.tvTotal.text = "Total Bayar: Rp ${item.totalBayar ?: 0}"

        if (item.status.equals("Proses", true) || item.status.equals("Menunggu", true)) {
            holder.btnAksi.visibility = View.VISIBLE
            holder.btnAksi.text = "Bayar Sekarang"
        } else {
            holder.btnAksi.visibility = View.GONE
        }

        val tanggalDiambilVal = item.tanggalDiambil
        if (tanggalDiambilVal != null && tanggalDiambilVal > 0L) {
            holder.tvDiambil.visibility = View.VISIBLE
            holder.tvDiambil.text = "Diambil Pada ${dateFormat.format(Date(tanggalDiambilVal))}"
        } else {
            holder.tvDiambil.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnAksi.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = listData.size
}
