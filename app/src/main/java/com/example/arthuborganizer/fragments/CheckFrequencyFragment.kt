package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.arthuborganizer.databinding.FragmentCheckFrequencyBinding

class CheckFrequencyFragment : Fragment() {
    private lateinit var binding : FragmentCheckFrequencyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFrequencyBinding.inflate(inflater, container, false)
        return binding.root
    }
}