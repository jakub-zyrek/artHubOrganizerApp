package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentTicketControlBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketControlFragment : Fragment() {
    private lateinit var binding : FragmentTicketControlBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var refEvents : DatabaseReference
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var barcodeScanner : CompoundBarcodeView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTicketControlBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_ticketControlFragment_to_checkTicketFragment)
        }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarTicketControl)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refEvents = database.getReference(sharedViewModel.idHouse).child("events").child(sharedViewModel.id)

        barcodeScanner = binding.barcodeScanner

        checkCameraPermission()

        barcodeScanner.decodeContinuous(callback)
        barcodeScanner.visibility = View.VISIBLE

        binding.btnScanTicketControl.setOnClickListener {
            binding.barcodeScanner.visibility = View.VISIBLE
            binding.barcodeScanner.resume()
        }
    }

    private val code = 100

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA),
                code
            )
        } else {
            // Permission already granted
            binding.barcodeScanner.resume()
        }
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            val resultOfScanning = result.toString()
            barcodeScanner.pause()
            barcodeScanner.visibility = View.GONE

            checkTicket(resultOfScanning)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private fun checkTicket(result : String) {
        refEvents.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var isTicket = false

                for (ticket in snapshot.child("buyPlaces").children) {
                    if (ticket.child("code").value.toString() == result) {
                        binding.tvEmailTicketControl.text = ticket.child("email").value.toString()
                        binding.tvTypeTicketControl.text = if (ticket.child("reduced").value as Boolean) {getString(R.string.reducedLabel)} else {getString(R.string.normalLabel)}
                        binding.tvPlaceTicketControl.text = getString(R.string.rowNumberLabel) + ticket.key.toString().split(" ")[0] + getString(R.string.placeNumberLabel) + ticket.key.toString().split(" ")[1]
                        binding.tvInformationTicketControl.setBackgroundResource(R.drawable.rounded_green_view)
                        binding.tvInformationTicketControl.text = getString(R.string.ticketIsActualLabel)

                        binding.nuLinearEmail.visibility = View.VISIBLE
                        binding.nuLinearTicket.visibility = View.VISIBLE
                        binding.tvPlaceTicketControl.visibility = View.VISIBLE
                        binding.tvScannedTicketControl.visibility = View.VISIBLE

                        if (ticket.child("scanned").value.toString() == "false") {
                            binding.tvScannedTicketControl.setTextColor(getColor(requireContext(), R.color.green))
                            binding.tvScannedTicketControl.text = getString(R.string.notScannedLabel)

                            refEvents.child("buyPlaces").child(ticket.key.toString()).child("scanned").setValue(SimpleDateFormat("dd.MM.yy HH:mm:ss", Locale.getDefault()).format(Date()))
                                .addOnCompleteListener {
                                    if (!it.isSuccessful) {
                                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            binding.tvScannedTicketControl.text = getString(R.string.scannedLabel) + ticket.child("scanned").value.toString()
                            binding.tvScannedTicketControl.setTextColor(getColor(requireContext(), R.color.red))
                        }

                        isTicket = true
                        break
                    }
                }

                if (!isTicket) {
                    binding.nuLinearEmail.visibility = View.INVISIBLE
                    binding.nuLinearTicket.visibility = View.INVISIBLE
                    binding.tvPlaceTicketControl.visibility = View.INVISIBLE
                    binding.tvScannedTicketControl.visibility = View.INVISIBLE
                    binding.tvInformationTicketControl.setBackgroundResource(R.drawable.rounded_red_view)
                    binding.tvInformationTicketControl.text = getString(R.string.ticketIsNotActualLabel)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }
}