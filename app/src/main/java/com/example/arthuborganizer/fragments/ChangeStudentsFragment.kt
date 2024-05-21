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
    private lateinit var refStudentsClass : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItem>
    private lateinit var adapter : RecyclerViewAdapter
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var listOfSelected : MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeStudentsBinding.inflate(inflater, container, false)

        when (sharedViewModel.typeOfClass) {
            "change" -> binding.navBar.tvNavBarLabel.text = getString(R.string.navNarChangeStrudents)
            "add" -> binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddStudents)
            "view" -> {
                binding.navBar.tvNavBarLabel.text = getString(R.string.navBarStudentsInfo)
                binding.btnAddChangeStudentsFragment.visibility = View.GONE
            }
        }

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        binding.btnAddChangeStudentsFragment.setOnClickListener {
            if (sharedViewModel.typeOfClass == "change") {
                navControl.navigate(R.id.action_changeStudentsFragment_to_addStudentFragment)
            } else if (sharedViewModel.typeOfClass == "add") {
                add()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refStudents = database.getReference(sharedViewModel.idHouse).child("students")
        navControl = Navigation.findNavController(view)
        refStudentsClass = database.getReference(sharedViewModel.idHouse).child("classes").child(sharedViewModel.id).child("students")

        if (!sharedViewModel.students && sharedViewModel.typeOfClass != "add" && sharedViewModel.typeOfClass != "view") {
            Toast.makeText(context, getString(R.string.ToastNoAccess), Toast.LENGTH_SHORT).show()
            back()
        }

        binding.rvChangeStudentsFragment.setHasFixedSize(true)
        binding.rvChangeStudentsFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        if (sharedViewModel.typeOfClass == "add") {
            listOfSelected = mutableListOf()
            adapter = RecyclerViewAdapter(mList, R.drawable.add, R.drawable.add_selected)
        } else {
            adapter = RecyclerViewAdapter(mList)
        }
        adapter.setListener(this)
        binding.rvChangeStudentsFragment.adapter = adapter

        getDataFromFirebase()
    }

    private fun getDataFromFirebase() {
        refStudents.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()

                if (sharedViewModel.typeOfClass == "change" || sharedViewModel.typeOfClass == "view") {
                    for (temp in snapshot.children) {
                        if (temp.child("email").value.toString() != "null") {
                            mList.add(RecyclerViewItem(temp.key.toString(), temp.child("surname").value.toString(), temp.child("name").value.toString()))
                        }
                    }

                    mList.sortBy { it.value1 }

                    adapter.notifyDataSetChanged()
                } else if (sharedViewModel.typeOfClass == "add") {
                    refStudentsClass.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshotClass: DataSnapshot) {
                            val listOfIndex : MutableList<String> = mutableListOf()

                            for (temp in snapshotClass.children) {
                                listOfIndex.add(temp.key.toString())
                            }

                            for (temp in snapshot.children) {
                                if (temp.key !in listOfIndex) {
                                    if (temp.child("email").value.toString() != "null") {
                                        mList.add(RecyclerViewItem(temp.key.toString(), temp.child("surname").value.toString(), temp.child("name").value.toString()))
                                    }
                                }
                            }

                            mList.sortWith(compareBy(RecyclerViewItem :: value1))

                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(item: RecyclerViewItem) {
        if (sharedViewModel.typeOfClass == "change" || sharedViewModel.typeOfClass == "view") {
            sharedViewModel.id = item.id
            navControl.navigate(R.id.action_changeStudentsFragment_to_editStudentFragment)
        } else if (sharedViewModel.typeOfClass == "add") {
            if (item.id !in listOfSelected) {
                listOfSelected.add(item.id)
            } else {
                listOfSelected.remove(item.id)
            }
        }
    }

    private fun back() {
        if (sharedViewModel.typeOfClass == "change") {
            if (sharedViewModel.role == "officeWorker") {
                navControl.navigate(R.id.action_changeStudentsFragment_to_officeWorkerMenuFragment)
            } else {
                navControl.navigate(R.id.action_changeStudentsFragment_to_adminMenuFragment)
            }
        } else if (sharedViewModel.typeOfClass == "add") {
            sharedViewModel.typeOfClass = "change"
            navControl.navigate(R.id.action_changeStudentsFragment_to_changeStudentsClassFragment)
        } else if (sharedViewModel.typeOfClass == "view") {
            sharedViewModel.typeOfClass = "change"
            navControl.navigate(R.id.action_changeStudentsFragment_to_workerMenuFragment)
        }
    }

    private fun add() {
        for (item in listOfSelected) {
            refStudentsClass.child(item).setValue(true)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        back()
    }
}