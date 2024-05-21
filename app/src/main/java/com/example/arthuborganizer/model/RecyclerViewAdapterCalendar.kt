package com.example.arthuborganizer.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.CalendarItemBinding


class RecyclerViewAdapterCalendar(private val list: MutableList<RecyclerViewCalendarItem>, private val image : Int) : RecyclerView.Adapter<RecyclerViewAdapterCalendar.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(val binding: CalendarItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var listener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = CalendarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = list[position]

        holder.binding.tvHour.text = currentItem.value1
        holder.binding.tvCalendar.text = currentItem.value2

        if (currentItem.frequency) {
            holder.binding.ivCalendar.setImageResource(image)
        } else {
            holder.binding.ivCalendar.setImageResource(R.drawable.close_white)
        }

        holder.binding.root.setOnClickListener {
            listener?.onItemClick(currentItem)
        }
    }

    fun setListener(listener: OnClickListener) {
        this.listener = listener
    }

    interface OnClickListener {
        fun onItemClick(item: RecyclerViewCalendarItem)
    }
}
