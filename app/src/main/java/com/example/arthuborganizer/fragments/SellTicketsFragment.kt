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
import com.example.arthuborganizer.databinding.FragmentSellTicketsBinding
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

class SellTicketsFragment : Fragment(), RecyclerViewAdapterCalendar.OnClickListener {
    private lateinit var binding : FragmentSellTicketsBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var adapter : RecyclerViewAdapterCalendar
    private lateinit var refEvents : DatabaseReference
    private lateinit var mList : MutableList<RecyclerViewCalendarItem>
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellTicketsBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarSellTickets)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refEvents = database.getReference(sharedViewModel.idHouse).child("events")

        if (!sharedViewModel.sellTicket) {
            Toast.makeText(context, getString(R.string.ToastNoAccess), Toast.LENGTH_SHORT).show()
            back()
        }

        binding.calendarSellTicketsFragment.setDate(sharedViewModel.selectedDateSellTicket.timeInMillis, false, true)
        var actualDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(sharedViewModel.selectedDateSellTicket.time)
        getDataFromFirebase(actualDate)

        binding.calendarSellTicketsFragment.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            sharedViewModel.selectedDateSellTicket = calendar
            actualDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            getDataFromFirebase(actualDate)
        }

        binding.rvSellTicketsFragment.setHasFixedSize(true)
        binding.rvSellTicketsFragment.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = RecyclerViewAdapterCalendar(mList, R.drawable.curtains_white)
        adapter.setListener(this)
        binding.rvSellTicketsFragment.adapter = adapter
    }

    private fun getDataFromFirebase(actualDate : String) {
        refEvents.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                binding.noClassSellTicketsFragment.visibility = View.GONE
                for (temp in snapshot.children) {
                    if (temp.child("date").value.toString() == actualDate) {
                        mList.add(RecyclerViewCalendarItem(temp.key.toString(), temp.child("hour").value.toString(), temp.child("name").value.toString(), temp.key.toString(), true))
                    }
                }

                if (mList.size == 0) {
                    binding.noClassSellTicketsFragment.visibility = View.VISIBLE
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
        navControl.navigate(R.id.action_sellTicketsFragment_to_sellTicketFragment)
    }

    private fun back() {
        navControl.navigate(R.id.action_sellTicketsFragment_to_officeWorkerMenuFragment)
    }
}