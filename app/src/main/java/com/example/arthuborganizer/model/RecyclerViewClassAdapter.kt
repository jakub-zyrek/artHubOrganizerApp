package com.example.arthuborganizer.model

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.ItemClassLayoutBinding
import com.example.arthuborganizer.databinding.ItemLayoutBinding
import com.example.arthuborganizer.fragments.AddChangeRoomsFragment
import com.example.arthuborganizer.fragments.AddClassFragment
import com.example.arthuborganizer.fragments.ChangeClassesFragment
import com.example.arthuborganizer.fragments.ChangeEventsFragment
import com.example.arthuborganizer.fragments.ChangeOfficeWorkersFragment
import com.example.arthuborganizer.fragments.ChangeStudentsClassFragment
import com.example.arthuborganizer.fragments.ChangeStudentsFragment
import com.example.arthuborganizer.fragments.ChangeWorkersFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class RecyclerViewClassAdapter(private val list : MutableList<RecyclerViewItem>, private val manager : FragmentManager) : RecyclerView.Adapter<RecyclerViewClassAdapter.RecyclerViewHolder>() {
    inner class RecyclerViewHolder(val binding: ItemClassLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    private val daysOfWeek : ArrayList<String> = arrayListOf()

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