package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentSellTicketBinding

class SellTicketFragment : Fragment() {
    private lateinit var binding : FragmentSellTicketBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellTicketBinding.inflate(inflater, container, false)
        return binding.root
    }
}