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
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentWorkerMenuBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class WorkerMenuFragment : Fragment() {
    private lateinit var binding : FragmentWorkerMenuBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var refWorker : DatabaseReference
    private lateinit var cultureHouseRef : DatabaseReference
    private lateinit var navControl : NavController
    private lateinit var databaseReference : DatabaseReference
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    val currentDateTime: Calendar = Calendar.getInstance()
    val calendar: Calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    val mList = hashMapOf<String, Date>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkerMenuBinding.inflate(inflater, container, false)

        binding.btnLogOutOfficeWorkerMenuFragment.setOnClickListener {
            auth.signOut()
            navControl.navigate(R.id.action_workerMenuFragment_to_startFragment)
        }

        binding.linearChangeStudentsWorkerMenuFragment.setOnClickListener {
            sharedViewModel.typeOfClass = "view"
            navControl.navigate(R.id.action_workerMenuFragment_to_changeStudentsFragment)
        }

        binding.linearCheckFrequencyWorkerMenuFragment.setOnClickListener {
            navControl.navigate(R.id.action_workerMenuFragment_to_checkFrequencyFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        refWorker = database.getReference("users/" + auth.currentUser!!.uid)
        navControl = Navigation.findNavController(view)

        databaseReference = database.getReference(sharedViewModel.idHouse).child("classes")

        classLabel()

        val timer = Timer()

        val task = object : TimerTask() {
            var previousMinute = Calendar.getInstance().get(Calendar.MINUTE)

            override fun run() {
                val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

                if (currentMinute != previousMinute) {
                    classLabel()
                    previousMinute = currentMinute
                }
            }
        }

        timer.schedule(task, 0, 60 * 1000)

        refWorker.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvNameWorkerMenuFragment.text = snapshot.child("name").value.toString() + "!"

                cultureHouseRef = database.getReference(snapshot.child("id").value.toString() + "/name")

                cultureHouseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.tvCultureHouseNameWorkerMenuFragment.text = snapshot.value.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }

                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun classLabel() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mList.clear()

                for (item in dataSnapshot.children) {
                    if (item.child("worker").value.toString() == auth.currentUser!!.uid) {
                        for (lesson in item.child("lessons").children) {
                            calendar.time = dateFormat.parse(lesson.child("date").value.toString() + " " + lesson.child("time").value.toString())!!

                            Log.d("d", calendar.time.toString())
                            if (calendar.time >= currentDateTime.time) {
                                mList[item.child("name").value.toString()] = calendar.time
                                break
                            }
                        }
                    }
                }

                val result = mList.minByOrNull { (_, value) -> value }

                binding.tvNameClassWorkerMenu.text = result!!.key
                binding.tvDateClassWorkerMenu.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(result!!.value)
                binding.tvTimeClassWorkerMenu.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(result.value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Wystąpił błąd podczas pobierania danych z Firebase: ${databaseError.message}")
            }
        })
    }
}