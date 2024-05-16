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
import com.example.arthuborganizer.databinding.FragmentAddStudentBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddStudentFragment : Fragment() {
    private lateinit var binding : FragmentAddStudentBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var ref : DatabaseReference
    private lateinit var navControl : NavController
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddStudentBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddStudent)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        binding.btnDateAddStudentFragment.setOnClickListener { showDatePicker() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        ref = database.getReference(sharedViewModel.idHouse).child("students")
        navControl = Navigation.findNavController(view)

        binding.btnAddAddStudentFragment.setOnClickListener {
            if (
                binding.etEmailAddStudentFragment.text.toString().isEmpty() ||
                binding.etNameAddStudentFragment.text.toString().isEmpty() ||
                binding.etPhoneAddStudentFragment.text.toString().isEmpty() ||
                binding.etSurnameAddStudentFragment.text.toString().isEmpty() ||
                binding.btnDateAddStudentFragment.text == getText(R.string.clickLabel)
            ) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else if (binding.etPhoneAddStudentFragment.text.toString().length != 9) {
                Toast.makeText(context, getString(R.string.ToastGoodPhoneNumber), Toast.LENGTH_SHORT).show()
            } else {
                val user = hashMapOf(
                    "email" to binding.etEmailAddStudentFragment.text.toString().trim(),
                    "name" to binding.etNameAddStudentFragment.text.toString().trim(),
                    "surname" to binding.etSurnameAddStudentFragment.text.toString().trim(),
                    "dateOfBirth" to binding.btnDateAddStudentFragment.text.toString(),
                    "phoneNumber" to binding.etPhoneAddStudentFragment.text.toString()
                )

                ref.push().setValue(user)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, getString(R.string.ToastAddStudent), Toast.LENGTH_SHORT).show()
                            back()
                        } else {
                            Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun showDatePicker() {
        val temp : Long

        if (binding.btnDateAddStudentFragment.text.toString() != getString(R.string.clickLabel)) {
            val calendar = Calendar.getInstance()
            val parseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(binding.btnDateAddStudentFragment.text.toString())
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
            binding.btnDateAddStudentFragment.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    private fun back() {
        navControl.navigate(R.id.action_addStudentFragment_to_changeStudentsFragment)
    }
}