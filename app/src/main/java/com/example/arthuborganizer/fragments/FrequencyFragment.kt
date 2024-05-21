package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentFrequencyBinding
import com.example.arthuborganizer.model.RecyclerViewAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FrequencyFragment : Fragment(), RecyclerViewAdapter.OnClickListener {
    private lateinit var binding : FragmentFrequencyBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var adapter : RecyclerViewAdapter
    private lateinit var ref : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItem>
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFrequencyBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_frequencyFragment_to_checkFrequencyFragment)
        }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarFrequency)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        ref = database.getReference(sharedViewModel.idHouse)

        binding.rvFrequencyFragment.setHasFixedSize(true)
        binding.rvFrequencyFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapter(mList, R.drawable.star, R.drawable.star_red)
        adapter.setListener(this)
        binding.rvFrequencyFragment.adapter = adapter

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val refClass = snapshot.child("classes").child(sharedViewModel.idClass)
                mList.clear()

                if (refClass.child("lessons").child(sharedViewModel.id).child("frequency").value.toString() == "null") {
                    for (student in refClass.child("students").children) {
                        mList.add(RecyclerViewItem(student.key.toString(), snapshot.child("students").child(student.key.toString()).child("surname").value.toString(), snapshot.child("students").child(student.key.toString()).child("name").value.toString()))
                    }
                } else {
                    for (student in refClass.child("lessons").child(sharedViewModel.id).child("frequency").children) {
                        mList.add(RecyclerViewItem(student.key.toString(), snapshot.child("students").child(student.key.toString()).child("surname").value.toString(), snapshot.child("students").child(student.key.toString()).child("name").value.toString(), student.value as Boolean))
                    }
                }

                mList.sortBy { it.value2 }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        binding.btnChangeFrequencyFragment.setOnClickListener {
            val frequency = hashMapOf<String, Boolean>()

            for (item in mList) {
                frequency[item.id] = item.present
            }

            ref.child("classes").child(sharedViewModel.idClass).child("lessons").child(sharedViewModel.id).child("frequency").setValue(frequency)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, getString(R.string.ToastChangeFrequency), Toast.LENGTH_SHORT).show()
                        navControl.navigate(R.id.action_frequencyFragment_to_checkFrequencyFragment)
                    } else {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onItemClick(item: RecyclerViewItem) {
        val index = mList.indexOf(RecyclerViewItem(item.id, item.value1, item.value2, item.present))

        if (index != -1) {
            mList[index] = RecyclerViewItem(item.id, item.value1, item.value2, !item.present)
            item.present = !item.present
        } else {
            Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
        }
    }
}