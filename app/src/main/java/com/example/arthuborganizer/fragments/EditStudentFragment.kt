package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentEditStudentBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditStudentFragment : Fragment() {
    private lateinit var binding : FragmentEditStudentBinding
    private lateinit var navControl : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var refStudent : DatabaseReference
    private lateinit var refClasses : DatabaseReference
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditStudentBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarEditStudent)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        refStudent = database.getReference(sharedViewModel.idHouse).child("students").child(sharedViewModel.id)
        refClasses = database.getReference(sharedViewModel.idHouse).child("classes")

        refStudent.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.btnDateEditStudent.text = snapshot.child("dateOfBirth").value.toString()

                binding.btnDateEditStudent.setOnClickListener { showDatePicker() }

                binding.etEmailEditStudentFragment.setText(snapshot.child("email").value.toString())
                binding.etNameEditStudentFragment.setText(snapshot.child("name").value.toString())
                binding.etNumberEditStudentFragment.setText(snapshot.child("phoneNumber").value.toString())
                binding.etSurnameEditStudentFragment.setText(snapshot.child("surname").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnChangeEditStudent.setOnClickListener {
            val temp = hashMapOf(
                "dateOfBirth" to binding.btnDateEditStudent.text.toString(),
                "email" to binding.etEmailEditStudentFragment.text.toString(),
                "name" to binding.etNameEditStudentFragment.text.toString(),
                "phoneNumber" to binding.etNumberEditStudentFragment.text.toString(),
                "surname" to binding.etSurnameEditStudentFragment.text.toString()
            )

            refStudent.setValue(temp)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, getString(R.string.ToastChangeStudent), Toast.LENGTH_SHORT).show()
                        back()
                    } else {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnDeleteEditStudent.setOnClickListener {
            refClasses.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // delete student from classes
                    for (item in snapshot.children) {
                        for (student in item.child("students").children) {
                            if (student.key == sharedViewModel.id) {
                                refClasses.child(item.key.toString()).child("students").child(student.key.toString()).removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(context, getString(R.string.ToastDeleteStudentFromClasses), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                    }

                    refStudent.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot1: DataSnapshot) {
                            val temp = hashMapOf(
                                "name" to snapshot1.child("name").value.toString(),
                                "surname" to snapshot1.child("surname").value.toString()
                            )

                            refStudent.setValue(temp)
                                .addOnCompleteListener {
                                    if (!it.isSuccessful) {
                                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, getString(R.string.ToastDeleteStudentWithout), Toast.LENGTH_SHORT).show()
                                        back()
                                    }
                                }
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
    }

    private fun showDatePicker() {
        val temp : Long

        if (binding.btnDateEditStudent.text != getString(R.string.selectButtonLabel)) {
            val calendar = Calendar.getInstance()
            val parseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(binding.btnDateEditStudent.text.toString())
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
            binding.btnDateEditStudent.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    private fun back() {
        navControl.navigate(R.id.action_editStudentFragment_to_changeStudentsFragment)
    }
}