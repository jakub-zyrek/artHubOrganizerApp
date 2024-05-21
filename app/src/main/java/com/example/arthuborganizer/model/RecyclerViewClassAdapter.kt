package com.example.arthuborganizer.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arthuborganizer.databinding.ItemClassLayoutBinding

class RecyclerViewClassAdapter(private val list : MutableList<RecyclerViewItem>) : RecyclerView.Adapter<RecyclerViewClassAdapter.RecyclerViewHolder>() {
    inner class RecyclerViewHolder(val binding: ItemClassLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemClassLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.day.text = this.value2
                binding.hour.text = this.value1
            }
        }
    }
}