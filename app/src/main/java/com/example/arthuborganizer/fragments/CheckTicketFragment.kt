package com.example.arthuborganizer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.arthuborganizer.databinding.FragmentCheckTicketBinding
import com.google.firebase.auth.FirebaseAuth


class CheckTicketFragment : Fragment() {
    private lateinit var binding : FragmentCheckTicketBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckTicketBinding.inflate(inflater, container, false)



        return binding.root
    }

}