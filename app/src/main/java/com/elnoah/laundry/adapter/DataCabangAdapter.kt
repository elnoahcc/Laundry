package com.elnoah.laundry.adapter

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
import com.elnoah.laundry.modeldata.modelcabang

class DataCabangAdapter(
    private val cabangList: ArrayList<modelcabang>,
    private val onViewClick: (modelcabang) -> Unit,
    private val onItemClick: (modelcabang) -> Unit
) : RecyclerView.Adapter<DataCabangAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdCabang: TextView = itemView.findViewById(R.id.tvIDCABANG)
        val tvNamaCabang: TextView = itemView.findViewById(R.id.tvNamaLokasiCabang)
        val tvAlamatCabang: TextView = itemView.findViewById(R.id.tvAlamatCabang)
        val tvTeleponCabang: TextView = itemView.findViewById(R.id.tvTeleponCabang)
        val tvTanggalTerdaftar: TextView = itemView.findViewById(R.id.tvTanggalTerdaftar)
        val btnView: Button = itemView.findViewById(R.id.btnLihat)
        val btnContact: Button = itemView.findViewById(R.id.btnContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cabang = cabangList[position]

        // Bind data ke views
        holder.tvIdCabang.text = cabang.idCabang ?: ""
        holder.tvNamaCabang.text = cabang.namaLokasiCabang ?: ""
        holder.tvAlamatCabang.text = cabang.alamatCabang ?: ""
        holder.tvTeleponCabang.text = cabang.teleponCabang ?: ""

        // Format tanggal terdaftar
        val tanggalText = if (cabang.tanggalTerdaftar.isNullOrEmpty()) {
            "Tanggal tidak tersedia"
        } else {
            "Terdaftar: ${cabang.tanggalTerdaftar}"
        }
        holder.tvTanggalTerdaftar.text = tanggalText

        // Click listener untuk CardView (edit dengan placeholder data)
        holder.itemView.setOnClickListener {
            onItemClick(cabang)
        }

        // Click listener untuk tombol Lihat (dialog detail)
        holder.btnView.setOnClickListener {
            onViewClick(cabang)
        }

        // Click listener untuk tombol Contact (hubungi via WhatsApp/telepon)
        holder.btnContact.setOnClickListener {
            handleContactClick(holder, cabang)
        }
    }

    private fun handleContactClick(holder: ViewHolder, cabang: modelcabang) {
        val context = holder.itemView.context
        val phoneNumber = cabang.teleponCabang?.replace(Regex("[^0-9+]"), "") // Keep + and numbers only

        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Format phone number for WhatsApp (remove leading 0, add 62 for Indonesia)
            val formattedNumber = formatPhoneNumberForWhatsApp(phoneNumber)

            val message = "Halo, saya ingin menanyakan informasi mengenai layanan laundry di cabang ${cabang.namaLokasiCabang}."
            val whatsappIntent = Intent(Intent.ACTION_VIEW)
            whatsappIntent.data = Uri.parse("https://wa.me/$formattedNumber?text=${Uri.encode(message)}")

            // Try to open WhatsApp first
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // Fallback to regular phone call if WhatsApp is not available
            try {
                val phoneIntent = Intent(Intent.ACTION_DIAL)
                phoneIntent.data = Uri.parse("tel:${cabang.teleponCabang}")
                context.startActivity(phoneIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Tidak dapat membuka aplikasi telepon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatPhoneNumberForWhatsApp(phoneNumber: String): String {
        return when {
            phoneNumber.startsWith("+62") -> phoneNumber.substring(3) // Remove +62
            phoneNumber.startsWith("62") -> phoneNumber.substring(2) // Remove 62
            phoneNumber.startsWith("0") -> "62${phoneNumber.substring(1)}" // Replace 0 with 62
            else -> "62$phoneNumber" // Add 62 prefix
        }
    }

    override fun getItemCount(): Int = cabangList.size

    // Method untuk update data (optional, jika diperlukan)
    fun updateData(newCabangList: List<modelcabang>) {
        cabangList.clear()
        cabangList.addAll(newCabangList)
        notifyDataSetChanged()
    }

    // Method untuk add single item (optional, jika diperlukan)
    fun addItem(cabang: modelcabang) {
        cabangList.add(0, cabang) // Add to top
        notifyItemInserted(0)
    }

    // Method untuk remove item (optional, jika diperlukan)
    fun removeItem(position: Int) {
        if (position in 0 until cabangList.size) {
            cabangList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}