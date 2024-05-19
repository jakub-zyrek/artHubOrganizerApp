package com.example.arthuborganizer.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentSellTicketBinding
import com.example.arthuborganizer.model.CreateQRAndSend
import com.example.arthuborganizer.model.RecyclerViewAdapterTicket
import com.example.arthuborganizer.model.RecyclerViewItemTicket
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class SellTicketFragment : Fragment(), RecyclerViewAdapterTicket.OnClickListener {
    private lateinit var binding : FragmentSellTicketBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var adapter : RecyclerViewAdapterTicket
    private lateinit var refEvents : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewItemTicket>
    private lateinit var selectedPlace : String
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellTicketBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_sellTicketFragment_to_sellTicketsFragment)
        }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarSellTicket)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refEvents = database.getReference(sharedViewModel.idHouse).child("events").child(sharedViewModel.id)

        binding.rvSellTicket.setHasFixedSize(true)
        binding.rvSellTicket.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapterTicket(mList, getString(R.string.reducedLabel), getString(R.string.normalLabel))
        adapter.setListener(this)
        binding.rvSellTicket.adapter = adapter

        setPlacesAdapter()

        numberAndPrice()

        refEvents.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.btnAddTicketSellTicketFragment.setOnClickListener {
                    if (binding.autoCompleteSellTicketFragment.text.toString() == "") {
                        Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
                    } else {
                        mList.add(RecyclerViewItemTicket(
                            selectedPlace,
                            binding.radioReducedTicketSellTicketFragment.isChecked,
                            snapshot.child(if (binding.radioReducedTicketSellTicketFragment.isChecked) {"priceReduced"} else {"priceNormal"}).value.toString()
                        ))

                        binding.autoCompleteSellTicketFragment.text.clear()

                        refEvents.child("buyPlaces").child(selectedPlace).child("email").setValue("block")
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(context, getString(R.string.ToastAddTicket), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                                }
                            }

                        adapter.notifyDataSetChanged()
                        numberAndPrice()
                    }
                }

                binding.btnSellTicket.setOnClickListener {
                    if (binding.etEmailSellTicketFragment.text!!.isEmpty() || binding.rvSellTicket.size == 0) {
                        Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
                    } else {
                        val time = snapshot.child("date").value.toString() + " " + snapshot.child("hour").value.toString()
                        val name = snapshot.child("name").value.toString()
                        val recipientEmail = binding.etEmailSellTicketFragment.text.toString().trim().lowercase()
                        var number = 1
                        val pdfs : MutableList<Pair<String, String>> = mutableListOf()
                        val codes : MutableList<String> = mutableListOf()

                        for (item in mList) {
                            val qrContent = CreateQRAndSend().generateRandomString(25)
                            codes.add(qrContent)
                            val qrImageFileName = "qr$number.png"
                            val pdfFileName = "ticket$number.pdf"

                            val place = getString(R.string.rowNumberLabel) + item.id.split(" ")[0] + getString(R.string.placeNumberLabel) + item.id.split(" ")[1]
                            val type = getString(R.string.ticketLabel) + " " + if (item.reduced) {getString(R.string.reducedLabel).lowercase()} else {getString(R.string.normalLabel).lowercase()}

                            val qrBitmap = CreateQRAndSend().generateQRCode(qrContent, 200, 200)
                            val qrImageFile = CreateQRAndSend().saveBitmapToFile(requireContext(), qrBitmap, qrImageFileName)
                            CreateQRAndSend().createPDFWithQRCode(requireContext(), qrBitmap, pdfFileName, name, time, place, type)

                            pdfs.add(Pair(File(context?.filesDir, pdfFileName).absolutePath, pdfFileName))

                            number++
                        }

                        val createQRAndSend = CreateQRAndSend()

                        val subject = getString(R.string.qr_code_pdf)
                        val body = getString(R.string.qr_code_pdf)

                        createQRAndSend.sendEmailUsingWorkManager(requireContext(), subject, body, recipientEmail, pdfs)

                        number = 0
                        var success = true

                        for (item in mList) {
                            val value = hashMapOf(
                                "email" to binding.etEmailSellTicketFragment.text.toString().trim().lowercase(),
                                "status" to true,
                                "code" to codes[number],
                                "scanned" to false,
                                "reduced" to item.reduced,
                                "price" to item.price
                            )

                            number++

                            refEvents.child("buyPlaces").child(item.id).setValue(value)
                                .addOnCompleteListener {
                                    if (!it.isSuccessful) {
                                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                                        success = false
                                    }
                                }
                        }

                        if (success) {
                            Toast.makeText(context, getString(R.string.ToastSellTicket), Toast.LENGTH_SHORT).show()
                            navControl.navigate(R.id.action_sellTicketFragment_to_sellTicketsFragment)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setPlacesAdapter() {
        val places : ArrayList<String> = arrayListOf()
        val placesId : ArrayList<String> = arrayListOf()
        selectedPlace = "null"

        refEvents.child("buyPlaces").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                places.clear()
                placesId.clear()

                for (item in snapshot.children) {
                    if (item.child("email").value.toString() == "null") {
                        placesId.add(item.key.toString())
                        places.add(getString(R.string.rowNumberLabel) + item.key.toString().split(" ")[0] + getString(R.string.placeNumberLabel) + item.key.toString().split(" ")[1])
                    }
                }

                binding.autoCompleteSellTicketFragment.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_item, places))

                binding.autoCompleteSellTicketFragment.setOnItemClickListener { _, _, position, _ ->
                    selectedPlace = placesId[position]
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(item: RecyclerViewItemTicket) {
        mList.remove(item)
        refEvents.child("buyPlaces").child(selectedPlace).child("email").removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, getString(R.string.ToastDelete), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                }
            }
        adapter.notifyDataSetChanged()
        numberAndPrice()
    }

    private fun numberAndPrice() {
        binding.tvNumberSellTicket.text = mList.size.toString()
        binding.tvPriceSellTicketFragment.text = mList.sumOf { it.price?.toDoubleOrNull() ?: 0.0 }.toString()
    }

}