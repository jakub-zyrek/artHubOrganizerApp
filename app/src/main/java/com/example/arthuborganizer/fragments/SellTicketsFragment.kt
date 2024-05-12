package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentSellTicketsBinding

class SellTicketsFragment : Fragment() {
    private lateinit var binding : FragmentSellTicketsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }
}