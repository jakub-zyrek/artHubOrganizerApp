package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentChangeOneClassBinding
import com.example.arthuborganizer.model.RecyclerViewAdapterCalendar
import com.example.arthuborganizer.model.RecyclerViewCalendarItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChangeOneClassFragment : Fragment(), RecyclerViewAdapterCalendar.OnClickListener {
    private lateinit var binding : FragmentChangeOneClassBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var auth : FirebaseAuth
    private lateinit var adapter : RecyclerViewAdapterCalendar
    private lateinit var refClasses : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewCalendarItem>
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var actualDate : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeOneClassBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_changeOneClassFragment_to_officeWorkerMenuFragment)
        }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarOneClass)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        refClasses = database.getReference(sharedViewModel.idHouse).child("classes")

        binding.calendarChangeOneClassFragment.setDate(sharedViewModel.selectedDate.timeInMillis, false, true)
        actualDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(sharedViewModel.selectedDate.time)
        getDataFromFirebase(actualDate)

        binding.calendarChangeOneClassFragment.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            sharedViewModel.selectedDate = calendar
            actualDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            getDataFromFirebase(actualDate)
        }

        binding.rvChangeOneClassFragment.setHasFixedSize(true)
        binding.rvChangeOneClassFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapterCalendar(mList, R.drawable.check1)
        adapter.setListener(this)
        binding.rvChangeOneClassFragment.adapter = adapter
    }

    private fun getDataFromFirebase(actualDate : String) {
        refClasses.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                binding.noClassCheckFrequencyFragment.visibility = View.GONE
                for (temp in snapshot.children) {
                    for (item in temp.child("lessons").children) {
                        if (item.child("date").value.toString() == actualDate) {
                            mList.add(RecyclerViewCalendarItem(item.key.toString(), item.child("time").value.toString(), temp.child("name").value.toString(), temp.key.toString(), item.child("frequency").value.toString() != "null"))
                        }
                    }
                }

                if (mList.size == 0) {
                    binding.noClassCheckFrequencyFragment.visibility = View.VISIBLE
                }

                mList.sortBy { it.value1 }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker() {
        val temp : Long

        if (binding.menuChangeOneClassFragment.btnDate.text.toString() != getString(R.string.dateLabel)) {
            val calendar = Calendar.getInstance()
            val parseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(binding.menuChangeOneClassFragment.btnDate.text.toString())
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
            binding.menuChangeOneClassFragment.btnDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showTimePicker() {

        val time : List<String> = if (binding.menuChangeOneClassFragment.btnTime.text.toString() != getString(R.string.hourLabel)) {
            binding.menuChangeOneClassFragment.btnTime.text.toString().split(":")
        } else {
            listOf("00", "00")
        }

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(time[0].toInt())
            .setMinute(time[1].toInt())
            .setTitleText(getString(R.string.TimePickerSelectHour))
            .build()

        timePicker.show(getChildFragmentManager(), "TimePickerTag")

        timePicker.addOnPositiveButtonClickListener {
            if (timePicker.minute < 10) {
                binding.menuChangeOneClassFragment.btnTime.text = timePicker.hour.toString() + ":0" + timePicker.minute.toString()
            } else {
                binding.menuChangeOneClassFragment.btnTime.text = timePicker.hour.toString() + ":" + timePicker.minute.toString()
            }
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onItemClick(item: RecyclerViewCalendarItem) {
        binding.menuChangeOneClassFragment.layoutChangeTimeClass.visibility = View.VISIBLE
        binding.menuChangeOneClassFragment.btnDate.text = actualDate
        binding.menuChangeOneClassFragment.btnTime.text = item.value1
        binding.menuChangeOneClassFragment.tvChangeTimeClass.text = getString(R.string.changeTimeLabel) + item.value2

        binding.menuChangeOneClassFragment.btnTime.setOnClickListener { showTimePicker() }
        binding.menuChangeOneClassFragment.btnDate.setOnClickListener { showDatePicker() }

        binding.menuChangeOneClassFragment.btnNoAddDeleteMenu.setOnClickListener {
            binding.menuChangeOneClassFragment.layoutChangeTimeClass.visibility = View.GONE
        }

        binding.menuChangeOneClassFragment.btnYesAddDeleteMenu.setOnClickListener {
            val data = mapOf(
                "date" to binding.menuChangeOneClassFragment.btnDate.text.toString(),
                "time" to binding.menuChangeOneClassFragment.btnTime.text.toString()
            )

            refClasses.child(item.classId).child("lessons").child(item.id).updateChildren(data)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, getString(R.string.ToastChangeTime), Toast.LENGTH_SHORT).show()
                        binding.menuChangeOneClassFragment.layoutChangeTimeClass.visibility = View.GONE
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}