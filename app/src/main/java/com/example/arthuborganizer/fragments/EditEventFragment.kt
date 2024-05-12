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
import com.example.arthuborganizer.databinding.FragmentEditEventBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
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

class EditEventFragment : Fragment() {
    private lateinit var binding : FragmentEditEventBinding
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var refRooms : DatabaseReference
    private lateinit var refEvent : DatabaseReference
    private lateinit var selectedRoom : String
    private lateinit var rooms : ArrayList<String>
    private lateinit var roomsId : ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditEventBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarEditEvent)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        binding.btnDateEditEventFragment.setOnClickListener { showDatePicker(binding.btnDateEditEventFragment.text.toString()) }

        binding.btnHourEditEventFragment.setOnClickListener { showTimePicker(binding.btnHourEditEventFragment.text.toString().split(":")) }

        binding.autoCompleteRoomEditEventFragment.inputType = InputType.TYPE_NULL

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refRooms = database.getReference(sharedViewModel.idHouse).child("rooms")
        refEvent = database.getReference(sharedViewModel.idHouse).child("events").child(sharedViewModel.id)

        refEvent.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.etNameEditEventFragment.setText(snapshot.child("name").value.toString())
                binding.etNormalTicketEditEventFragment.setText(snapshot.child("priceNormal").value.toString())
                binding.etReducedTicketEditEventFragment.setText(snapshot.child("priceReduced").value.toString())
                binding.btnDateEditEventFragment.text = snapshot.child("date").value.toString()
                binding.btnHourEditEventFragment.text = snapshot.child("hour").value.toString()

                setRoomsAdapter(snapshot.child("room").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnEditEditEventFragment.setOnClickListener {
            refEvent.child("name").setValue(binding.etNameEditEventFragment.text.toString())
            refEvent.child("date").setValue(binding.btnDateEditEventFragment.text.toString())
            refEvent.child("priceNormal").setValue(binding.etNormalTicketEditEventFragment.text.toString())
            refEvent.child("priceReduced").setValue(binding.etReducedTicketEditEventFragment.text.toString())
            refEvent.child("hour").setValue(binding.btnHourEditEventFragment.text.toString())
            refEvent.child("room").setValue(selectedRoom)

            Toast.makeText(requireContext(), getString(R.string.ToastEditEvent), Toast.LENGTH_SHORT).show()
            back()
        }

        binding.btnDeleteEditEventFragment.setOnClickListener {
            refEvent.removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), getString(R.string.ToastDeleteEvent), Toast.LENGTH_SHORT).show()
                        back()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setRoomsAdapter(room : String) {
        rooms = arrayListOf()
        roomsId = arrayListOf()
        var temp = -1

        refRooms.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    temp++

                    if (room == item.key.toString()) {
                        val adapterAutoCompleteRooms = ArrayAdapter(requireContext(), R.layout.spinner_item, rooms)
                        binding.autoCompleteRoomEditEventFragment.setText(item.value.toString())
                        binding.autoCompleteRoomEditEventFragment.setAdapter(adapterAutoCompleteRooms)
                        selectedRoom = item.key.toString()
                    }

                    rooms.add(item.value.toString())
                    roomsId.add(item.key.toString())
                }

                binding.autoCompleteRoomEditEventFragment.setOnItemClickListener { _, _, position, _ ->
                    selectedRoom = roomsId[position]
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showDatePicker(date : String) {
        val calendar = Calendar.getInstance()
        val parseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date)
        calendar.time = parseDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.DatePickerSelectData))
            .setSelection(calendar.timeInMillis)
            .build()

        datePicker.show(getChildFragmentManager(), "DatePickerTag")

        datePicker.addOnPositiveButtonClickListener {
            binding.btnDateEditEventFragment.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showTimePicker(time : List<String>) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(time[0].toInt())
            .setMinute(time[1].toInt())
            .setTitleText(getString(R.string.TimePickerSelectHour))
            .build()

        timePicker.show(getChildFragmentManager(), "TimePickerTag")

        timePicker.addOnPositiveButtonClickListener {
            if (timePicker.minute.toString() == "0") {
                binding.btnHourEditEventFragment.text = timePicker.hour.toString() + ":00"
            } else {
                binding.btnHourEditEventFragment.text = timePicker.hour.toString() + ":" + timePicker.minute.toString()
            }
        }
    }

    private fun back() {
        navControl.navigate(R.id.action_editEventFragment_to_changeEventsFragment)
    }
}