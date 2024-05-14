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
import com.example.arthuborganizer.databinding.FragmentChangeOfficeWorkersBinding
import com.example.arthuborganizer.model.RecyclerViewAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangeOfficeWorkersFragment : Fragment(), RecyclerViewAdapter.OnClickListener {
    private lateinit var binding : FragmentChangeOfficeWorkersBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItem>
    private lateinit var adapter : RecyclerViewAdapter
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeOfficeWorkersBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarChangeOfficeWorkers)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_changeOfficeWorkersFragment_to_adminMenuFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("users")
        navControl = Navigation.findNavController(view)

        binding.rvChangeOfficeWorkersFragment.setHasFixedSize(true)
        binding.rvChangeOfficeWorkersFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapter(mList)
        adapter.setListener(this)
        binding.rvChangeOfficeWorkersFragment.adapter = adapter

        getDataFromFirebase()
    }

    override fun onItemClick(item: RecyclerViewItem) {
        sharedViewModel.id = item.id
        navControl.navigate(R.id.action_changeOfficeWorkersFragment_to_editOfficeWorkerFragment)
    }

    private fun getDataFromFirebase() {
        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (temp in snapshot.children) {
                    if (temp.child("role").value == "officeWorker" && temp.child("id").value == sharedViewModel.idHouse && temp.child("password").value.toString() == "null") {
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