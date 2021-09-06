package com.luproject.dollarapplication.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luproject.dollarapplication.R
import kotlinx.android.synthetic.main.cell_photo.view.*
import java.util.*

class PhotoAdapter (private val data: ArrayList<Uri>): RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_photo,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.imageView.setImageURI(data[position])

    }

    override fun getItemCount() = data.size


    fun addPhoto(uri: Uri){
        data.add(0,uri)
        notifyItemInserted(0)
    }
}