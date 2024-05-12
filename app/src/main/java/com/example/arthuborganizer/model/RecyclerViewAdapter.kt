package com.example.arthuborganizer.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arthuborganizer.databinding.ItemLayoutBinding

class RecyclerViewAdapter(private val list: MutableList<RecyclerViewItem>) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var listener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = list[position]

        holder.binding.tvFullNameItem.text = (currentItem.value1 + " " + currentItem.value2).trim()

        holder.binding.root.setOnClickListener {
            listener?.onItemClick(currentItem)
        }
    }

    fun setListener(listener: OnClickListener) {
        this.listener = listener
    }

    interface OnClickListener {
        fun onItemClick(item: RecyclerViewItem)
    }
}
