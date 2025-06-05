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
import com.elnoah.laundry.modeldata.modelpegawai
import com.google.android.material.button.MaterialButton

class DataPegawaiAdapter(
    private val pegawaiList: ArrayList<modelpegawai>,
    private val onItemClick: (modelpegawai) -> Unit,
    private val onDeleteClick: ((modelpegawai) -> Unit)? = null
) : RecyclerView.Adapter<DataPegawaiAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataIDPegawai: TextView = itemView.findViewById(R.id.tvDataIDPegawai)
        val tvDataNamaPegawai: TextView = itemView.findViewById(R.id.tvDataNamaPegawai)
        val tvDataAlamatPegawai: TextView = itemView.findViewById(R.id.tvDataAlamatPegawai)
        val tvDataNoHpPegawai: TextView = itemView.findViewById(R.id.tvDataNoHpPegawai)
        val tvDataCabangPegawai: TextView = itemView.findViewById(R.id.tvDataCabangPegawai)
        val tvDataTerdaftarPegawai: TextView = itemView.findViewById(R.id.tvDataTerdaftarPegawai)
        val btDataHubungiPegawai: Button = itemView.findViewById(R.id.btDataHubungiPegawai)
        val btnDataLihatPegawai: Button = itemView.findViewById(R.id.btnDataLihatPegawai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pegawai = pegawaiList[position]

        holder.tvDataIDPegawai.text = pegawai.idPegawai ?: "N/A"
        holder.tvDataNamaPegawai.text = pegawai.namaPegawai ?: "Nama tidak tersedia"
        holder.tvDataAlamatPegawai.text = pegawai.alamatPegawai ?: "Alamat tidak tersedia"
        holder.tvDataNoHpPegawai.text = pegawai.noHPPegawai ?: "No HP tidak tersedia"
        holder.tvDataCabangPegawai.text = pegawai.cabangPegawai ?: "Cabang tidak tersedia"
        holder.tvDataTerdaftarPegawai.text = pegawai.tanggalTerdaftar ?: "Tanggal tidak tersedia"

        // Tombol Hubungi - membuka WhatsApp atau dialer
        holder.btDataHubungiPegawai.setOnClickListener {
            val phoneNumber = pegawai.noHPPegawai
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
                Toast.makeText(holder.itemView.context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol Lihat - menampilkan dialog detail
        holder.btnDataLihatPegawai.setOnClickListener {
            showDetailDialog(holder.itemView.context, pegawai)
        }

        // Click pada item untuk edit
        holder.itemView.setOnClickListener {
            onItemClick(pegawai)
        }
    }

    override fun getItemCount(): Int = pegawaiList.size

    private fun showDetailDialog(context: Context, pegawai: modelpegawai) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_mod_pegawai)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind data ke dialog
        val tvIdPegawai = dialog.findViewById<TextView>(R.id.tvDataIDPegawai)
        val tvNama = dialog.findViewById<TextView>(R.id.tvDataNamaPegawai)
        val tvAlamat = dialog.findViewById<TextView>(R.id.tvDataAlamatPegawai)
        val tvNoHP = dialog.findViewById<TextView>(R.id.tvDataNoHpPegawai)
        val tvCabang = dialog.findViewById<TextView>(R.id.tvDataCabangPegawai)
        val tvTerdaftar = dialog.findViewById<TextView>(R.id.tvDataTerdaftarPegawai)
        val btnEdit = dialog.findViewById<MaterialButton>(R.id.btn_edit_pegawai)
        val btnDelete = dialog.findViewById<MaterialButton>(R.id.btn_delete_pegawai)

        // Set data
        tvIdPegawai.text = pegawai.idPegawai ?: "N/A"
        tvNama.text = pegawai.namaPegawai ?: "Nama tidak tersedia"
        tvAlamat.text = pegawai.alamatPegawai ?: "Alamat tidak tersedia"
        tvNoHP.text = pegawai.noHPPegawai ?: "No HP tidak tersedia"
        tvCabang.text = pegawai.cabangPegawai ?: "Cabang tidak tersedia"
        tvTerdaftar.text = pegawai.tanggalTerdaftar ?: "Tanggal tidak tersedia"

        // Tombol Edit
        btnEdit.setOnClickListener {
            dialog.dismiss()
            onItemClick(pegawai) // Memanggil fungsi edit yang sudah ada
        }

        // Tombol Delete
        btnDelete.setOnClickListener {
            dialog.dismiss()
            // Panggil callback delete jika tersedia
            onDeleteClick?.invoke(pegawai)
        }

        dialog.show()
    }
}