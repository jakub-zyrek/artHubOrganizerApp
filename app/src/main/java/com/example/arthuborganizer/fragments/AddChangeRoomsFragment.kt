package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentAddChangeRoomsBinding
import com.example.arthuborganizer.model.RecyclerViewAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddChangeRoomsFragment : Fragment(), RecyclerViewAdapter.OnClickListener {
    private lateinit var binding: FragmentAddChangeRoomsBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var mList: MutableList<RecyclerViewItem>
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var navControl: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddChangeRoomsBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddChangeRooms)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_addChangeRoomsFragment_to_adminMenuFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(auth.currentUser!!.uid).child("rooms")
        navControl = Navigation.findNavController(view)

        mList = mutableListOf()
        adapter = RecyclerViewAdapter(mList)
        adapter.setListener(this)
        binding.rvAddChangeRoomsFragment.adapter = adapter
        binding.rvAddChangeRoomsFragment.layoutManager = LinearLayoutManager(context)

        binding.btnAddChangeRoomsFragment.setOnClickListener {
            binding.addDeleteMenuAddChangeRooms.layoutAddDeleteRoomMenu.visibility = View.VISIBLE

            binding.addDeleteMenuAddChangeRooms.btnYesAddDeleteMenu.setOnClickListener {
                myRef.push().setValue(binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.text.toString().trim())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, getString(R.string.ToastAddRoom), Toast.LENGTH_SHORT).show()
                            binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.text?.clear()
                            binding.addDeleteMenuAddChangeRooms.layoutAddDeleteRoomMenu.visibility = View.GONE
                        } else {
                            Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            binding.addDeleteMenuAddChangeRooms.btnNoAddDeleteMenu.setOnClickListener {
                binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.text?.clear()
                binding.addDeleteMenuAddChangeRooms.layoutAddDeleteRoomMenu.visibility = View.GONE
            }
        }


        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (temp in snapshot.children) {
                    mList.add(RecyclerViewItem(temp.key.toString(), temp.value.toString(), ""))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }
        })

        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val newItem = RecyclerViewItem(snapshot.key.toString(), snapshot.value.toString(), "")
                if (!mList.contains(newItem)) {
                    mList.add(newItem)
                    adapter.notifyItemInserted(mList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val itemId = snapshot.key.toString()
                val newItem = RecyclerViewItem(itemId, snapshot.value.toString(), "")
                val index = mList.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    mList[index] = newItem
                    adapter.notifyItemChanged(index)
                }
            }

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
    }

    override fun onItemClick(item: RecyclerViewItem) {
        binding.addDeleteMenuAddChangeRooms.layoutAddDeleteRoomMenu.visibility = View.VISIBLE

        binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.setText(item.value1)

        binding.addDeleteMenuAddChangeRooms.btnYesAddDeleteMenu.setOnClickListener {
            myRef.child(item.id).setValue(binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context,
                            getString(R.string.ToastChangeName), Toast.LENGTH_SHORT).show()
                        binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.text?.clear()
                        binding.addDeleteMenuAddChangeRooms.layoutAddDeleteRoomMenu.visibility = View.GONE
                    } else {
                        Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.addDeleteMenuAddChangeRooms.btnNoAddDeleteMenu.setOnClickListener {
            myRef.child(item.id).removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, getString(R.string.ToastDeleteRoom), Toast.LENGTH_SHORT).show()
                        binding.addDeleteMenuAddChangeRooms.etNameAddDeleteMenu.text?.clear()
                        binding.addDeleteMenuAddChangeRooms.layoutAddDeleteRoomMenu.visibility = View.GONE
                    } else {
                        Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
