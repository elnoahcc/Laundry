package com.elnoah.laundry.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelpelanggan
import com.google.android.material.button.MaterialButton

class DataPelangganAdapter(
    private val pelangganList: ArrayList<modelpelanggan>,
    private val onItemClick: (modelpelanggan) -> Unit,
    private val onDeleteClick: ((modelpelanggan) -> Unit)? = null
) : RecyclerView.Adapter<DataPelangganAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataIDPelanggan: TextView = itemView.findViewById(R.id.tvDataIDPelanggan)
        val tvDataNamaPelanggan: TextView = itemView.findViewById(R.id.tvDataNamaPelanggan)
        val tvDataAlamatPelanggan: TextView = itemView.findViewById(R.id.tvDataAlamatPelanggan)
        val tvDataNoHpPelanggan: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvDataCabangPelanggan: TextView = itemView.findViewById(R.id.tvDataCabangPelanggan)
        val tvDataTerdaftarPelanggan: TextView = itemView.findViewById(R.id.tvDataTerdaftarPelanggan)
        val btDataHubungiPelanggan: Button = itemView.findViewById(R.id.btDataHubungiPelanggan)
        val btnDataLihatPelanggan: Button = itemView.findViewById(R.id.btnDataLihatPelanggan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelanggan = pelangganList[position]

        holder.tvDataIDPelanggan.text = pelanggan.idPelanggan ?: "N/A"
        holder.tvDataNamaPelanggan.text = pelanggan.namaPelanggan ?: "Nama tidak tersedia"
        holder.tvDataAlamatPelanggan.text = pelanggan.alamatPelanggan ?: "Alamat tidak tersedia"
        holder.tvDataNoHpPelanggan.text = pelanggan.noHPPelanggan ?: "No HP tidak tersedia"
        holder.tvDataCabangPelanggan.text = pelanggan.cabangPelanggan ?: "Cabang tidak tersedia"
        holder.tvDataTerdaftarPelanggan.text = pelanggan.tanggalTerdaftar ?: "Tanggal tidak tersedia"

        // Tombol Hubungi - membuka WhatsApp atau dialer
        holder.btDataHubungiPelanggan.setOnClickListener {
            val phoneNumber = pelanggan.noHPPelanggan
            if (!phoneNumber.isNullOrEmpty()) {
                try {
                    // Coba buka WhatsApp terlebih dahulu
                    val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/${phoneNumber.removePrefix("0")}")
                    }
                    holder.itemView.context.startActivity(whatsappIntent)
                } catch (e: Exception) {
                    // Jika WhatsApp tidak tersedia, buka dialer
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    holder.itemView.context.startActivity(dialIntent)
                }
            } else {
                Toast.makeText(  holder.itemView.context,
                    holder.itemView.context.getString(R.string.telepon_tidak_tersedia),
                    Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol Lihat - menampilkan dialog detail
        holder.btnDataLihatPelanggan.setOnClickListener {
            showDetailDialog(holder.itemView.context, pelanggan)
        }

        // Click pada item untuk edit
        holder.itemView.setOnClickListener {
            onItemClick(pelanggan)
        }
    }

    override fun getItemCount(): Int = pelangganList.size

    private fun showDetailDialog(context: Context, pelanggan: modelpelanggan) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_mod_pelanggan)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind data ke dialog
        val tvIdPelanggan = dialog.findViewById<TextView>(R.id.tv_ID_PELANGGAN)
        val tvNama = dialog.findViewById<TextView>(R.id.tv_NAMA)
        val tvAlamat = dialog.findViewById<TextView>(R.id.tv_ALAMAT)
        val tvNoHP = dialog.findViewById<TextView>(R.id.tv_NoHP)
        val tvCabang = dialog.findViewById<TextView>(R.id.tv_Cabang)
        val tvTerdaftar = dialog.findViewById<TextView>(R.id.tv_Terdaftar)
        val btnEdit = dialog.findViewById<MaterialButton>(R.id.btn_edit)
        val btnDelete = dialog.findViewById<MaterialButton>(R.id.btn_delete)

        // Set data
        tvIdPelanggan.text = pelanggan.idPelanggan ?: "N/A"
        tvNama.text = pelanggan.namaPelanggan ?: "Nama tidak tersedia"
        tvAlamat.text = pelanggan.alamatPelanggan ?: "Alamat tidak tersedia"
        tvNoHP.text = pelanggan.noHPPelanggan ?: "No HP tidak tersedia"
        tvCabang.text = pelanggan.cabangPelanggan ?: "Cabang tidak tersedia"
        tvTerdaftar.text = pelanggan.tanggalTerdaftar ?: "Tanggal tidak tersedia"

        // Tombol Edit
        btnEdit.setOnClickListener {
            dialog.dismiss()
            onItemClick(pelanggan) // Memanggil fungsi edit yang sudah ada
        }

        // Tombol Delete
        btnDelete.setOnClickListener {
            dialog.dismiss()
            // Panggil callback delete jika tersedia
            onDeleteClick?.invoke(pelanggan)
        }

        dialog.show()
    }
}