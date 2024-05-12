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
import androidx.core.util.Pair
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentAddClassBinding
import com.example.arthuborganizer.model.RecyclerViewClassAdapter
import com.example.arthuborganizer.model.RecyclerViewItem
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

class AddClassFragment : Fragment() {
    private lateinit var binding : FragmentAddClassBinding
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var refRooms : DatabaseReference
    private lateinit var refWorkers : DatabaseReference
    private lateinit var selectedWorker : String
    private lateinit var selectedRoom : String
    private lateinit var mList : MutableList<RecyclerViewItem>
    private lateinit var adapter : RecyclerViewClassAdapter
    private var firstDate = Calendar.getInstance()
    private var secondDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddClassBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddClass)

        binding.navBar.ivNavBarBack.setOnClickListener {
            if (sharedViewModel.role == "officeWorker") {
                navControl.navigate(R.id.action_addClassFragment_to_officeWorkerMenuFragment)
            } else {
                navControl.navigate(R.id.action_addClassFragment_to_adminMenuFragment)
            }
        }

        binding.btnDateAddClassFragment.setOnClickListener {
            showDatePicker()
        }

        binding.autoCompleteRoomAddClassFragment.inputType = InputType.TYPE_NULL
        binding.autoCompleteTypeAddClassFragment.inputType = InputType.TYPE_NULL
        binding.autoCompleteWorkerAddClassFragment.inputType = InputType.TYPE_NULL
        binding.autoCompleteDaySpinnerClass.inputType = InputType.TYPE_NULL

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(sharedViewModel.idHouse).child("classes")
        refRooms = database.getReference(sharedViewModel.idHouse).child("rooms")
        refWorkers = database.getReference("users")

        if (!sharedViewModel.clas) {
            Toast.makeText(context, getString(R.string.ToastNoAccess), Toast.LENGTH_SHORT).show()
            navControl.navigate(R.id.action_addClassFragment_to_officeWorkerMenuFragment)
        }

        setRoomsAdapter()

        setWorkersAdapter()

        binding.autoCompleteTypeAddClassFragment.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_item, arrayOf(getString(R.string.weekLabel), getString(R.string.monthLabel))))

        recycledViewAdapter()

        timePicker()

        val daysOfWeek = arrayOf(getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wendesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday), getString(R.string.sundey))

        binding.autoCompleteDaySpinnerClass.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_item, daysOfWeek))

        binding.btnAddAddClassFragment.setOnClickListener {
            if (
                binding.etNameAddClassFragment.text!!.isEmpty() ||
                selectedRoom == "null" ||
                binding.autoCompleteTypeAddClassFragment.text!!.isEmpty() ||
                selectedWorker == "null" ||
                binding.rvAddClassFragment.size == 0 ||
                binding.etNumberAddClassFragment.text!!.isEmpty() ||
                binding.btnDateAddClassFragment.text == getString(R.string.selectButtonLabel)
            ) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else {
                val temp1 : HashMap<String, Any> = hashMapOf()

                var number = 1
                var firstDateChange : Calendar

                for (x in mList) {
                    firstDateChange = firstDate.clone() as Calendar

                    var dayOfMonth = -1

                    when (x.value2) {
                        daysOfWeek[6] -> dayOfMonth = 1
                        daysOfWeek[0] -> dayOfMonth = 2
                        daysOfWeek[1] -> dayOfMonth = 3
                        daysOfWeek[2] -> dayOfMonth = 4
                        daysOfWeek[3] -> dayOfMonth = 5
                        daysOfWeek[4] -> dayOfMonth = 6
                        daysOfWeek[5] -> dayOfMonth = 7
                    }

                    var valueToAdd = dayOfMonth - firstDateChange.get(Calendar.DAY_OF_WEEK)

                    if (valueToAdd < 0) valueToAdd += 6

                    firstDateChange.add(Calendar.DAY_OF_MONTH, valueToAdd)

                    temp1[number.toString()] = hashMapOf(
                        "date" to SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(firstDateChange.time).toString(),
                        "time" to x.value1,
                        "display" to true
                    )

                    valueToAdd = 7 * binding.etNumberAddClassFragment.text.toString().toInt()

                    if (binding.autoCompleteTypeAddClassFragment.text.toString() == getString(R.string.monthLabel)) valueToAdd *= 4

                    firstDateChange.add(Calendar.DAY_OF_MONTH, valueToAdd)

                    while (secondDate > firstDateChange) {
                        number++

                        temp1[number.toString()] = hashMapOf(
                            "date" to SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(firstDateChange.time).toString(),
                            "time" to x.value1,
                            "display" to true
                        )

                        firstDateChange.add(Calendar.DAY_OF_MONTH, valueToAdd)
                    }
                }

                val temp = hashMapOf(
                    "name" to binding.etNameAddClassFragment.text.toString(),
                    "room" to selectedRoom,
                    "worker" to selectedWorker,
                    "lessons" to temp1
                )

                myRef.push().setValue(temp)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context,
                                getString(R.string.ToastAddClass), Toast.LENGTH_SHORT).show()

                            if (sharedViewModel.role == "officeWorker") {
                                navControl.navigate(R.id.action_addClassFragment_to_officeWorkerMenuFragment)
                            } else {
                                navControl.navigate(R.id.action_addClassFragment_to_adminMenuFragment)
                            }
                        }
                    }
            }
        }

        binding.btnAddDayAddClassFragment.setOnClickListener {
            if (binding.btnHourSpinnerClass.text.toString() != getString(R.string.hourLabel) && binding.autoCompleteDaySpinnerClass.text.toString().isNotEmpty()) {
                mList.add(RecyclerViewItem("", binding.btnHourSpinnerClass.text.toString(), binding.autoCompleteDaySpinnerClass.text.toString()))

                adapter.notifyDataSetChanged()

                binding.btnHourSpinnerClass.text = getString(R.string.hourLabel)
                binding.autoCompleteDaySpinnerClass.text.clear()
            } else {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun recycledViewAdapter() {
        binding.rvAddClassFragment.setHasFixedSize(true)
        binding.rvAddClassFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewClassAdapter(mList, getChildFragmentManager())
        binding.rvAddClassFragment.adapter = adapter

    }

    private fun setWorkersAdapter() {
        val workers : ArrayList<String> = arrayListOf()
        val workersId : ArrayList<String> = arrayListOf()
        selectedWorker = "null"

        refWorkers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    if (item.child("role").value.toString() == "worker" && item.child("id").value.toString() == sharedViewModel.idHouse) {
                        workers.add(item.child("name").value.toString() + " " + item.child("surname").value.toString())
                        workersId.add(item.key.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        val adapterAutoCompleteWorkers = ArrayAdapter(requireContext(), R.layout.spinner_item, workers)

        binding.autoCompleteWorkerAddClassFragment.setAdapter(adapterAutoCompleteWorkers)

        binding.autoCompleteWorkerAddClassFragment.setOnItemClickListener { _, _, position, _ ->
            selectedWorker = workersId[position]
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

        binding.autoCompleteRoomAddClassFragment.setAdapter(adapterAutoCompleteRooms)

        binding.autoCompleteRoomAddClassFragment.setOnItemClickListener { _, _, position, _ ->
            selectedRoom = roomsId[position]
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePicker() {
        val firstTemp : Long
        val secondTemp : Long

        if (binding.btnDateAddClassFragment.text.toString() != getString(R.string.selectButtonLabel)) {
            val firstCalendar = Calendar.getInstance()
            val secondCalendar = Calendar.getInstance()

            val firstParseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(binding.btnDateAddClassFragment.text.toString().split(" - ")[0])
            firstCalendar.time = firstParseDate!!
            firstCalendar.add(Calendar.DAY_OF_MONTH, 1)

            val secondParseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(binding.btnDateAddClassFragment.text.toString().split(" - ")[1])
            secondCalendar.time = secondParseDate!!
            secondCalendar.add(Calendar.DAY_OF_MONTH, 1)

            firstTemp = firstCalendar.timeInMillis
            secondTemp = secondCalendar.timeInMillis
        } else {
            firstTemp = MaterialDatePicker.thisMonthInUtcMilliseconds()
            secondTemp = MaterialDatePicker.todayInUtcMilliseconds()
        }

        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.DatePickerSelectData))
            .setSelection(Pair(firstTemp, secondTemp))
            .build()

        datePicker.show(getChildFragmentManager(), "DatePickerTag")

        datePicker.addOnPositiveButtonClickListener {
            firstDate.time = Date(it.first)
            secondDate.time = Date(it.second)

            val dataFirst = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it.first))
            val dataSecond = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it.second))

            binding.btnDateAddClassFragment.text = "$dataFirst - $dataSecond"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun timePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(12)
            .setMinute(10)
            .setTitleText(getString(R.string.TimePickerSelectHour))
            .build()

        binding.btnHourSpinnerClass.setOnClickListener {
            timePicker.show(getChildFragmentManager(), "tag")
        }

        timePicker.addOnPositiveButtonClickListener {
            if (timePicker.minute.toString() == "0") {
                binding.btnHourSpinnerClass.text = timePicker.hour.toString() + ":00"
            } else {
                binding.btnHourSpinnerClass.text = timePicker.hour.toString() + ":" + timePicker.minute.toString()
            }
        }
    }
}