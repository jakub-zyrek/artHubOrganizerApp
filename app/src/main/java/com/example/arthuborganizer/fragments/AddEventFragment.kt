package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentAddEventBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventFragment : Fragment() {
    private lateinit var binding : FragmentAddEventBinding
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var refEvents : DatabaseReference
    private lateinit var refRooms : DatabaseReference
    private lateinit var selectedRoom : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEventBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddEvent)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }
        binding.btnDateAddEventFragment.setOnClickListener { showDatePicker() }
        binding.btnHourAddEventFragment.setOnClickListener { showTimePicker() }

        binding.autoCompleteRoomAddEventFragment.inputType = InputType.TYPE_NULL

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refEvents = database.getReference(sharedViewModel.idHouse).child("events")
        refRooms = database.getReference(sharedViewModel.idHouse).child("rooms")

        if (!sharedViewModel.event) {
            Toast.makeText(context, getString(R.string.ToastNoAccess), Toast.LENGTH_SHORT).show()
            navControl.navigate(R.id.action_addEventFragment_to_officeWorkerMenuFragment)
        }

        setRoomsAdapter()

        binding.btnAddAddEventFragment.setOnClickListener {
            if (
                binding.etNameAddEventFragment.text!!.isEmpty() ||
                binding.etNormalTicketAddEventFragment.text!!.isEmpty() ||
                binding.etRowsAddEventFragment.text!!.isEmpty() ||
                binding.etPlacesInRowAddEventFragment.text!!.isEmpty() ||
                binding.etReducedTicketAddEventFragment.text!!.isEmpty() ||
                selectedRoom == "null" ||
                binding.btnDateAddEventFragment.text == getString(R.string.dateLabel) ||
                binding.btnHourAddEventFragment.text == getString(R.string.hourLabel)
            ) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else {
                val places = hashMapOf<String, Any>()
                var a : String

                for (i in 1..binding.etRowsAddEventFragment.text.toString().toInt()) {
                    for (j in 1..binding.etPlacesInRowAddEventFragment.text.toString().toInt()) {
                        a = "$i $j"
                        places[a] = hashMapOf(
                            "status" to false
                        )
                    }
                }

                val temp = hashMapOf(
                    "name" to binding.etNameAddEventFragment.text.toString(),
                    "priceNormal" to binding.etNormalTicketAddEventFragment.text.toString(),
                    "priceReduced" to binding.etReducedTicketAddEventFragment.text.toString(),
                    "date" to binding.btnDateAddEventFragment.text,
                    "hour" to binding.btnHourAddEventFragment.text,
                    "room" to selectedRoom,
                    "buyPlaces" to places
                )

                val myRefPushing = refEvents.push()

                myRefPushing.setValue(temp)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, getString(R.string.ToastAddEvent), Toast.LENGTH_SHORT).show()

                            back()
                        }
                    }
            }
        }
    }

    private fun showDatePicker() {
        val temp : Long

        if (binding.btnDateAddEventFragment.text.toString() != getString(R.string.dateLabel)) {
            val calendar = Calendar.getInstance()
            val parseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(binding.btnDateAddEventFragment.text.toString())
            calendar.time = parseDate!!
            calendar.add(Calendar.DAY_OF_MONTH, 1)

            temp = calendar.timeInMillis
        } else {
            temp = MaterialDatePicker.todayInUtcMilliseconds()
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.DatePickerSelectData))
            .setSelection(temp)
            .build()

        datePicker.show(getChildFragmentManager(), "DatePickerTag")

        datePicker.addOnPositiveButtonClickListener {
            binding.btnDateAddEventFragment.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showTimePicker() {

        val time : List<String> = if (binding.btnHourAddEventFragment.text.toString() != getString(R.string.hourLabel)) {
            binding.btnHourAddEventFragment.text.toString().split(":")
        } else {
            listOf("00", "00")
        }

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(INPUT_MODE_CLOCK)
            .setHour(time[0].toInt())
            .setMinute(time[1].toInt())
            .setTitleText(getString(R.string.TimePickerSelectHour))
            .build()

        timePicker.show(getChildFragmentManager(), "TimePickerTag")

        timePicker.addOnPositiveButtonClickListener {
            if (timePicker.minute < 10) {
                binding.btnHourAddEventFragment.text = timePicker.hour.toString() + ":0" + timePicker.minute.toString()
            } else {
                binding.btnHourAddEventFragment.text = timePicker.hour.toString() + ":" + timePicker.minute.toString()
            }
        }
    }

    private fun back() {
        if (sharedViewModel.role == "officeWorker") {
            navControl.navigate(R.id.action_addEventFragment_to_officeWorkerMenuFragment)
        } else {
            navControl.navigate(R.id.action_addEventFragment_to_adminMenuFragment)
        }
    }

    private fun setRoomsAdapter() {
        val rooms : ArrayList<String> = arrayListOf()
        val roomsId : ArrayList<String> = arrayListOf()
        selectedRoom = "null"

        refRooms.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    rooms.add(item.value.toString())
                    roomsId.add(item.key.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        val adapterAutoCompleteRooms = ArrayAdapter(requireContext(), R.layout.spinner_item, rooms)

        binding.autoCompleteRoomAddEventFragment.setAdapter(adapterAutoCompleteRooms)

        binding.autoCompleteRoomAddEventFragment.setOnItemClickListener { _, _, position, _ ->
            selectedRoom = roomsId[position]
        }
    }
}