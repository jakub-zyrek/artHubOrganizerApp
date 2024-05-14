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
import com.example.arthuborganizer.databinding.FragmentChangeStudentsBinding
import com.example.arthuborganizer.model.RecyclerViewAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangeStudentsFragment : Fragment(), RecyclerViewAdapter.OnClickListener {
    private lateinit var binding : FragmentChangeStudentsBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var refStudents : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItem>
    private lateinit var adapter : RecyclerViewAdapter
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeStudentsBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navNarChangeStrudents)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        binding.btnAddChangeStudentsFragment.setOnClickListener {
            navControl.navigate(R.id.action_changeStudentsFragment_to_addStudentFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refStudents = database.getReference(sharedViewModel.idHouse).child("students")
        navControl = Navigation.findNavController(view)

        if (!sharedViewModel.students) {
            Toast.makeText(context, getString(R.string.ToastNoAccess), Toast.LENGTH_SHORT).show()
            back()
        }

        binding.rvChangeStudentsFragment.setHasFixedSize(true)
        binding.rvChangeStudentsFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapter(mList)
        adapter.setListener(this)
        binding.rvChangeStudentsFragment.adapter = adapter

        getDataFromFirebase()
    }

    private fun getDataFromFirebase() {
        refStudents.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (temp in snapshot.children) {
                    if (temp.child("email").value.toString() != "null") {
                        mList.add(RecyclerViewItem(temp.key.toString(), temp.child("name").value.toString(), ""))
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(item: RecyclerViewItem) {
        sharedViewModel.id = item.id
        navControl.navigate(R.id.action_changeStudentsFragment_to_editStudentFragment)
    }

    private fun back() {
        if (sharedViewModel.role == "officeWorker") {
            navControl.navigate(R.id.action_changeStudentsFragment_to_officeWorkerMenuFragment)
        } else {
            navControl.navigate(R.id.action_changeStudentsFragment_to_adminMenuFragment)
        }
    }
}