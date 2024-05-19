package com.example.arthuborganizer.model

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.ItemLayoutBinding
import com.example.arthuborganizer.databinding.TicketItemBinding

class RecyclerViewAdapterTicket(private val list: MutableList<RecyclerViewItemTicket>, private val reduced : String, private val normal : String) : RecyclerView.Adapter<RecyclerViewAdapterTicket.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(val binding: TicketItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var listener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = TicketItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = list[position]

        holder.binding.place.text = currentItem.id
        if (currentItem.reduced) {
            holder.binding.type.text = reduced
        } else {
            holder.binding.type.text = normal
        }

        holder.binding.price.text = currentItem.price

        holder.binding.ivDeleteTicketItem.setOnClickListener {
            listener?.onItemClick(currentItem)
        }
    }

    fun setListener(listener: OnClickListener) {
        this.listener = listener
    }

    interface OnClickListener {
        fun onItemClick(item: RecyclerViewItemTicket)
    }
}
