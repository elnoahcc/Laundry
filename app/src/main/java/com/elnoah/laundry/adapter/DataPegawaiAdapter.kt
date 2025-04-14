
package com.elnoah.laundry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelpegawai
import com.elnoah.laundry.pegawai.TambahPegawai
import com.google.firebase.database.DatabaseReference

class DataPegawaiAdapter(private val listPegawai: List<modelpegawai>) :
    RecyclerView.Adapter<DataPegawaiAdapter.ViewHolder>() {
        lateinit var appContext: Context
        lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]
        holder.tvID.text = item.idPegawai
        holder.tvNama.text = item.namaPegawai
        holder.tvAlamat.text = item.alamatPegawai
        holder.tvNoHP.text = item.noHPPegawai
        holder.tvTerdaftar.text = item.terdaftar
        holder.cvCARDPEGAWAI.setOnClickListener {
            val intent = Intent(appContext, TambahPegawai::class.java)
            intent.putExtra("Judul", "Edit Pegawai")
            intent.putExtra("idPegawai", item.idPegawai)
            intent.putExtra("namaPegawai", item.namaPegawai)
            intent.putExtra("noHPPegawai", item.noHPPegawai)
            intent.putExtra("alamatPegawai", item.alamatPegawai)
            intent.putExtra("idCabang", item.idCabang)
            appContext.startActivity(intent)
        }
        holder.btHubungi.setOnClickListener {

        }
        holder.btLihatPegawai.setOnClickListener {

        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCARDPEGAWAI: View = itemView.findViewById(R.id.CVDATA_PEGAWAI)
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDPegawai)
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaPegawai)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPegawai)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvDataNoHpPegawai)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tvDataTerdaftarPegawai)
        val btHubungi: Button = itemView.findViewById(R.id.btDataHubungiPegawai)
        val btLihatPegawai: Button = itemView.findViewById(R.id.btDataLihatPegawai)
    }
    override fun getItemCount(): Int {
        return listPegawai.size
    }
}