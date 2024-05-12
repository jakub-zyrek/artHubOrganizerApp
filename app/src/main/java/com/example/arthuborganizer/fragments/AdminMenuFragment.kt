package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentAdminMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminMenuFragment : Fragment() {
    private lateinit var binding : FragmentAdminMenuBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var cultureHouseRef : DatabaseReference
    private lateinit var userRef : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminMenuBinding.inflate(inflater, container, false)

        binding.linearAddOfficeWorkerAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_addOfficeWorkerFragment)
        }

        binding.btnLogOutAdminMenuFragment.setOnClickListener {
            auth.signOut()
            navControl.navigate(R.id.action_adminMenuFragment_to_startFragment)
        }

        binding.linearChangeOfficeWorkerAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_changeOfficeWorkersFragment)
        }

        binding.linearAddWorkerAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_addWorkerFragment)
        }

        binding.linearChangeWorkerAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_changeWorkersFragment)
        }

        binding.linearChangeCultureHouseAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_addChangeRoomsFragment)
        }

        binding.linearAddClassAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_addClassFragment)
        }

        binding.linearChangeClassAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_changeClassesFragment)
        }

        binding.linearAddEventAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_addEventFragment)
        }

        binding.linearChangeEventAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_changeEventsFragment)
        }

        binding.linearChangeStudentsAdminMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_adminMenuFragment_to_changeStudentsFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        cultureHouseRef = database.getReference(auth.currentUser!!.uid).child("name")
        userRef = database.getReference("users").child(auth.currentUser!!.uid).child("name")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvNameAdminMenuFragment.text = snapshot.value.toString() + "!"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        cultureHouseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvCultureHouseNameAdminMenuFragment.text = snapshot.value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }
}