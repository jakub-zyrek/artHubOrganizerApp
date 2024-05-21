package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentNoneCultureHouseBinding
import com.google.firebase.auth.FirebaseAuth

class NoneCultureHouseFragment : Fragment() {
    private lateinit var binding : FragmentNoneCultureHouseBinding
    private lateinit var navControl : NavController
    private lateinit var auth : FirebaseAuth
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