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
import com.example.arthuborganizer.databinding.FragmentWorkerMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkerMenuFragment : Fragment() {
    private lateinit var binding : FragmentWorkerMenuBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var refWorker : DatabaseReference
    private lateinit var cultureHouseRef : DatabaseReference
    private lateinit var navControl : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkerMenuBinding.inflate(inflater, container, false)

        binding.btnLogOutOfficeWorkerMenuFragment.setOnClickListener {
            auth.signOut()
            navControl.navigate(R.id.action_workerMenuFragment_to_startFragment)
        }

        binding.linearChangeStudentsWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_workerMenuFragment_to_changeStudentsFragment)
        }

        binding.linearCheckFrequencyWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_workerMenuFragment_to_checkFrequencyFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        refWorker = database.getReference("users/" + auth.currentUser!!.uid)
        navControl = Navigation.findNavController(view)

        refWorker.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvNameWorkerMenuFragment.text = snapshot.child("name").value.toString() + "!"

                cultureHouseRef = database.getReference(snapshot.child("id").value.toString() + "/name")

                cultureHouseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.tvCultureHouseNameWorkerMenuFragment.text = snapshot.value.toString()
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