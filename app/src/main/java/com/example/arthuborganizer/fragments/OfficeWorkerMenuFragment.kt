package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentOfficeWorkerMenuBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class OfficeWorkerMenuFragment : Fragment() {
    private lateinit var binding : FragmentOfficeWorkerMenuBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var refUser : DatabaseReference
    private lateinit var cultureHouseRef : DatabaseReference
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOfficeWorkerMenuBinding.inflate(inflater, container, false)

        binding.btnLogOutOfficeWorkerMenuFragment.setOnClickListener {
            auth.signOut()
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_startFragment)
        }

        binding.linearAddEventOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_addEventFragment)
        }

        binding.linearAddClassOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_addClassFragment)
        }

        binding.linearChangeClassOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_changeClassesFragment)
        }

        binding.linearChangeEventOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_changeEventsFragment)
        }

        binding.linearChangeStudentsOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_changeStudentsFragment)
        }

        binding.linearSellTicketsOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_sellTicketsFragment)
        }

        binding.linearCheckTicketsOfficeWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_officeWorkerMenuFragment_to_checkTicketFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        refUser = database.getReference("users/" + auth.currentUser!!.uid)
        navControl = Navigation.findNavController(view)

        refUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.child("name").value.toString() + "!"
                binding.tvNameOfficeWorkerMenuFragment.text = temp

                sharedViewModel.checkTicket = snapshot.child("access").child("checkTicket").value as Boolean
                sharedViewModel.clas = snapshot.child("access").child("class").value as Boolean
                sharedViewModel.event = snapshot.child("access").child("event").value as Boolean
                sharedViewModel.sellTicket = snapshot.child("access").child("sellTicket").value as Boolean
                sharedViewModel.students = snapshot.child("access").child("students").value as Boolean

                cultureHouseRef = database.getReference(snapshot.child("id").value.toString() + "/name")

                cultureHouseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.tvCultureHouseNameOfficeWorkerMenuFragment.text = snapshot.value.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }

                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

    }
}