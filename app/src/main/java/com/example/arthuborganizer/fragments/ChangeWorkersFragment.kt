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
import com.example.arthuborganizer.databinding.FragmentChangeWorkersBinding
import com.example.arthuborganizer.model.RecyclerViewAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangeWorkersFragment : Fragment(), RecyclerViewAdapter.OnClickListener {
    private lateinit var binding : FragmentChangeWorkersBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var refWorkers : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItem>
    private lateinit var adapter : RecyclerViewAdapter
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeWorkersBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarChangeWorkers)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_changeWorkersFragment_to_adminMenuFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refWorkers = database.getReference("users")
        navControl = Navigation.findNavController(view)

        binding.rvChangeWorkersFragment.setHasFixedSize(true)
        binding.rvChangeWorkersFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapter(mList)
        adapter.setListener(this)
        binding.rvChangeWorkersFragment.adapter = adapter

        getDataFromFirebase()
    }

    override fun onItemClick(item: RecyclerViewItem) {
        sharedViewModel.id = item.id
        navControl.navigate(R.id.action_changeWorkersFragment_to_editWorkerFragment)
    }

    private fun getDataFromFirebase() {
        refWorkers.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (temp in snapshot.children) {
                    if (temp.child("role").value == "worker" && sharedViewModel.idHouse == temp.child("id").value) {
                        mList.add(RecyclerViewItem(temp.key.toString(), temp.child("name").value.toString(), temp.child("surname").value.toString()))
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }
}