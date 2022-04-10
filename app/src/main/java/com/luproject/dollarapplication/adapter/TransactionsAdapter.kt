package com.luproject.dollarapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luproject.dollarapplication.R
import com.luproject.dollarapplication.models.Transaction
import kotlinx.android.synthetic.main.cell_transaction.view.*
import java.util.*

class TransactionsAdapter(private val data:ArrayList<Transaction>,
                          private val onTransactionClick:(Transaction)->Unit)
     : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {


    class ViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_transaction,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tvFrom.text = data[position].from
        holder.itemView.tvAmount1.text = data[position].amount.toString()
        holder.itemView.tvCurrency.text = data[position].currency
        holder.itemView.tvLatitude.text = data[position].lat.toString()
        holder.itemView.tvLongitude.text = data[position].lng.toString()
        holder.itemView.tvDate.text = data[position].date
        holder.itemView.setOnClickListener {
            onTransactionClick.invoke(data[position])
        }

    }

    fun addTransaction(t:Transaction){
        data.add(0,t)
        notifyItemInserted(0)
    }

    override fun getItemCount() = data.size
}