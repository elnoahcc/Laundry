package com.elnoah.laundry.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.elnoah.laundry.R
import com.elnoah.laundry.modeldata.modelpelanggan
import org.w3c.dom.Text


class DataPelangganAdapter(private val listPelanggan: ArrayList<modelpelanggan>) : RecyclerView.Adapter<DataPelangganAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val item = listPelanggan[position]
        holder.tvID.text = item.idPelanggan
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = item.alamatPelanggan
        holder.tvNoHP.text = item.noHPPelanggan
        holder.tvTerdaftar.text = item.terdaftar
        holder.cvCARD.setOnClickListener(){

        }
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCARD = itemView.findViewById<View>(R.id.CVDATA_PELANGGAN)
        val tvID = itemView.findViewById<TextView>(R.id.tvDataIDPelanggan)
        val tvNama = itemView.findViewById<TextView>(R.id.tvDataNamaPelanggan)
        val tvAlamat = itemView.findViewById<TextView>(R.id.tvDataAlamatPelanggan)
        val tvNoHP = itemView.findViewById<TextView>(R.id.tvDataNoHpPelanggan)
        val tvTerdaftar = itemView.findViewById<TextView>(R.id.tvDataTerdaftarPelanggan)
        val btHubungi = itemView.findViewById<Button>(R.id.btDataHubungiPelanggan)
        val btLihat = itemView.findViewById<Button>(R.id.btDataLihatPelanggan)
        val idCabang = itemView.findViewById<TextView>(R.id.tvDataCabangLayanan)
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }
}

