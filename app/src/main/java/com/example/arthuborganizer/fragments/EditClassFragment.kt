package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentEditClassBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditClassFragment : Fragment() {
    private lateinit var binding : FragmentEditClassBinding
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var refClasses : DatabaseReference
    private lateinit var refRooms : DatabaseReference
    private lateinit var refWorkers : DatabaseReference
    private lateinit var selectedWorker : String
    private lateinit var selectedRoom : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditClassBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarEditClass)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_editClassFragment_to_changeClassesFragment)
        }

        binding.changeStudentsEditClassFragment.setOnClickListener {
            navControl.navigate(R.id.action_editClassFragment_to_changeStudentsClassFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refClasses = database.getReference(sharedViewModel.idHouse).child("classes")
        refRooms = database.getReference(sharedViewModel.idHouse).child("rooms")
        refWorkers = database.getReference("users")

        setRoomsAdapter()

        setWorkersAdapter()

        binding.editEditClassFragment.setOnClickListener {
            if (
                binding.etNameEditClassFragment.text!!.isEmpty() ||
                selectedRoom == "null" ||
                selectedWorker == "null"
            ) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else {
                refClasses.child(sharedViewModel.id).child("name").setValue(binding.etNameEditClassFragment.text.toString())
                refClasses.child(sharedViewModel.id).child("room").setValue(selectedRoom)
                refClasses.child(sharedViewModel.id).child("worker").setValue(selectedWorker)

                Toast.makeText(context, getString(R.string.ToastEditClass), Toast.LENGTH_SHORT).show()
                navControl.navigate(R.id.action_editClassFragment_to_changeClassesFragment)
            }
        }

    }

    private fun setWorkersAdapter() {
        val workers : ArrayList<String> = arrayListOf()
        val workersId : ArrayList<String> = arrayListOf()

        refWorkers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    if (item.child("role").value.toString() == "worker" && item.child("id").value.toString() == sharedViewModel.idHouse) {
                        workers.add(item.child("name").value.toString() + " " + item.child("surname").value.toString())
                        workersId.add(item.key.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        val adapterAutoCompleteWorkers = ArrayAdapter(requireContext(), R.layout.spinner_item, workers)

        binding.autoCompleteWorkerEditClassFragment.setAdapter(adapterAutoCompleteWorkers)

        binding.autoCompleteWorkerEditClassFragment.setOnItemClickListener { _, _, position, _ ->
            selectedWorker = workersId[position]
        }
    }

    private fun setRoomsAdapter() {
        val rooms : ArrayList<String> = arrayListOf()
        val roomsId : ArrayList<String> = arrayListOf()
        selectedRoom = "null"

        refRooms.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    rooms.add(item.value.toString())
                    roomsId.add(item.key.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        val adapterAutoCompleteRooms = ArrayAdapter(requireContext(), R.layout.spinner_item, rooms)

        binding.autoCompleteRoomEditClassFragment.setAdapter(adapterAutoCompleteRooms)

        binding.autoCompleteRoomEditClassFragment.setOnItemClickListener { _, _, position, _ ->
            selectedRoom = roomsId[position]
        }
    }
}