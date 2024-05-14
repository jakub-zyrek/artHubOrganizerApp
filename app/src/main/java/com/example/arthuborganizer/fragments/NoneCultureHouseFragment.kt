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
import com.example.arthuborganizer.databinding.FragmentNoneCultureHouseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoneCultureHouseFragment : Fragment() {
    private lateinit var binding : FragmentNoneCultureHouseBinding
    private lateinit var navControl : NavController
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var userRef : DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoneCultureHouseBinding.inflate(inflater, container, false)

        binding.btnLogOutNoneCultureHouseHouse.setOnClickListener {
            auth.signOut()
            navControl.navigate(R.id.action_noneCultureHouseFragment_to_startFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }
}