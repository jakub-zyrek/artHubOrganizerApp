package com.example.arthuborganizer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.example.arthuborganizer.databinding.FragmentCheckFrequencyBinding
import com.example.arthuborganizer.model.RecyclerViewAdapterCalendar
import com.example.arthuborganizer.model.RecyclerViewCalendarItem
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CheckFrequencyFragment : Fragment(), RecyclerViewAdapterCalendar.OnClickListener {
    private lateinit var binding : FragmentCheckFrequencyBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var auth : FirebaseAuth
    private lateinit var adapter : RecyclerViewAdapterCalendar
    private lateinit var refClasses : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewCalendarItem>
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFrequencyBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_checkFrequencyFragment_to_workerMenuFragment)
        }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarCheckFrequency)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        refClasses = database.getReference(sharedViewModel.idHouse).child("classes")


        binding.calendarCheckFrequencyFragment.setDate(sharedViewModel.selectedDate.timeInMillis, false, true)
        var actualDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(sharedViewModel.selectedDate.time)
        getDataFromFirebase(actualDate)

        binding.calendarCheckFrequencyFragment.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            sharedViewModel.selectedDate = calendar
            actualDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            getDataFromFirebase(actualDate)
        }

        binding.rvCheckFrequencyFragment.setHasFixedSize(true)
        binding.rvCheckFrequencyFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapterCalendar(mList, R.drawable.check1)
        adapter.setListener(this)
        binding.rvCheckFrequencyFragment.adapter = adapter
    }

    private fun getDataFromFirebase(actualDate : String) {
        refClasses.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                binding.noClassCheckFrequencyFragment.visibility = View.GONE
                for (temp in snapshot.children) {
                    if (temp.child("worker").value.toString() == auth.currentUser?.uid.toString()) {
                        for (item in temp.child("lessons").children) {
                            if (item.child("date").value.toString() == actualDate) {
                                mList.add(RecyclerViewCalendarItem(item.key.toString(), item.child("time").value.toString(), temp.child("name").value.toString(), temp.key.toString(), item.child("frequency").value.toString() != "null"))
                            }
                        }
                    }
                }

                if (mList.size == 0) {
                    binding.noClassCheckFrequencyFragment.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onItemClick(item: RecyclerViewCalendarItem) {
        sharedViewModel.id = item.id
        sharedViewModel.idClass = item.classId
        navControl.navigate(R.id.action_checkFrequencyFragment_to_frequencyFragment)
    }
}