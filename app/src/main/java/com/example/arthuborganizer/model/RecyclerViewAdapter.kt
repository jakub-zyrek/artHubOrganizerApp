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


class RecyclerViewAdapter(private val list: MutableList<RecyclerViewItem>, private val imageId: Int = R.drawable.edit, private val imageSelected: Int? = null) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = list[position]

        holder.binding.tvFullNameItem.text = (currentItem.value1 + " " + currentItem.value2).trim()
        holder.binding.image.setImageResource(imageId)

        holder.binding.root.setOnClickListener {
            listener?.onItemClick(currentItem)
            if (imageSelected != null) {
                if (holder.binding.tvFullNameItem.typeface != null && holder.binding.tvFullNameItem.typeface.isBold) {
                    holder.binding.tvFullNameItem.setTypeface(null, Typeface.NORMAL)
                    holder.binding.image.setImageResource(imageId)
                } else {
                    holder.binding.tvFullNameItem.setTypeface(null, Typeface.BOLD)
                    holder.binding.image.setImageResource(imageSelected)
                }
            }
        }
    }

    fun setListener(listener: OnClickListener) {
        this.listener = listener
    }

    interface OnClickListener {
        fun onItemClick(item: RecyclerViewItem)
    }
}
