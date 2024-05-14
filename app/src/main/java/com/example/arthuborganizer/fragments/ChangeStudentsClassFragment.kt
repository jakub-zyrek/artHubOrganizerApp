package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.example.arthuborganizer.databinding.FragmentChangeStudentsClassBinding
import com.example.arthuborganizer.model.RecyclerViewAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangeStudentsClassFragment : Fragment(), RecyclerViewAdapter.OnClickListener {
    private lateinit var binding : FragmentChangeStudentsClassBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var refStudentsClass : DatabaseReference
    private lateinit var refStudent : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItem>
    private lateinit var adapter : RecyclerViewAdapter
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeStudentsClassBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navNarChangeStrudents)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refStudent = database.getReference(sharedViewModel.idHouse).child("students")
        refStudentsClass = database.getReference(sharedViewModel.idHouse).child("classes").child(sharedViewModel.id).child("students")
        navControl = Navigation.findNavController(view)

        if (!sharedViewModel.clas) {
            Toast.makeText(context, getString(R.string.ToastNoAccess), Toast.LENGTH_SHORT).show()
            back()
        }

        binding.rvChangeStudentsClassFragment.setHasFixedSize(true)
        binding.rvChangeStudentsClassFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapter(mList, R.drawable.bin)
        adapter.setListener(this)
        binding.rvChangeStudentsClassFragment.adapter = adapter

        getDataFromFirebase()

        refStudentsClass.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                refStudent.child(snapshot.key.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshotStudent: DataSnapshot) {
                        val newItem = RecyclerViewItem(snapshot.key.toString(), snapshotStudent.child("name").value.toString(), snapshotStudent.child("surname").value.toString())

                        if (!mList.contains(newItem)) {
                            mList.add(newItem)
                            adapter.notifyItemInserted(mList.size - 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val itemId = snapshot.key.toString()
                val index = mList.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    mList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.btnAddChangeStudentsClassFragment.setOnClickListener {
            sharedViewModel.typeOfClass = "add"
            navControl.navigate(R.id.action_changeStudentsClassFragment_to_changeStudentsFragment)
        }
    }

    private fun getDataFromFirebase() {
        refStudentsClass.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (temp in snapshot.children) {
                    Log.d("true", temp.key.toString())
                    refStudent.child(temp.key.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshotStudent: DataSnapshot) {
                            mList.add(RecyclerViewItem(snapshotStudent.key.toString(), snapshotStudent.child("name").value.toString(), snapshotStudent.child("surname").value.toString()))
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
        binding.deleteMenuChangeStudentsClassFragment.layoutDeleteMenu.visibility = View.VISIBLE

        binding.deleteMenuChangeStudentsClassFragment.btnYesDeleteMenu.setOnClickListener {
            refStudentsClass.child(item.id).removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, getString(R.string.ToastDeleteStudent), Toast.LENGTH_SHORT).show()
                        binding.deleteMenuChangeStudentsClassFragment.layoutDeleteMenu.visibility = View.GONE
                    } else {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.deleteMenuChangeStudentsClassFragment.btnNoDeleteMenu.setOnClickListener {
            binding.deleteMenuChangeStudentsClassFragment.layoutDeleteMenu.visibility = View.GONE
        }
    }

    private fun back() {
        navControl.navigate(R.id.action_changeStudentsClassFragment_to_editClassFragment)
    }
}