package com.example.arthuborganizer.fragments

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentEditOfficeWorkerBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditOfficeWorkerFragment : Fragment() {
    private lateinit var binding : FragmentEditOfficeWorkerBinding
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var database : FirebaseDatabase
    private lateinit var refOfficeWorker : DatabaseReference
    private lateinit var navControl : NavController
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditOfficeWorkerBinding.inflate(inflater, container, false)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_editOfficeWorkerFragment_to_changeOfficeWorkersFragment)
        }

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarEditWorker)

        binding.etEmailEditOfficeWorkerFragment.inputType = InputType.TYPE_NULL

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refOfficeWorker = database.getReference("users").child(sharedViewModel.id)
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()

        refOfficeWorker.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.etEmailEditOfficeWorkerFragment.setText(snapshot.child("email").value.toString())
                binding.etNameEditOfficeWorkerFragment.setText(snapshot.child("name").value.toString())
                binding.etSurnameEditOfficeWorkerFragment.setText(snapshot.child("surname").value.toString())
                binding.switchClassEditOfficeWorkerFragment.isChecked = snapshot.child("access").child("class").value as Boolean
                binding.switchEventEditOfficeWorkerFragment.isChecked = snapshot.child("access").child("event").value as Boolean
                binding.switchStudentsEditOfficeWorkerFragment.isChecked = snapshot.child("access").child("students").value as Boolean
                binding.switchCheckTicketsEditOfficeWorkerFragment.isChecked = snapshot.child("access").child("checkTicket").value as Boolean
                binding.switchSellTicketsEditOfficeWorkerFragment.isChecked = snapshot.child("access").child("sellTicket").value as Boolean
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        binding.btnEditEditOfficeWorkerFragment.setOnClickListener {
            val temp = hashMapOf(
                "email" to binding.etEmailEditOfficeWorkerFragment.text.toString(),
                "name" to binding.etNameEditOfficeWorkerFragment.text.toString(),
                "surname" to binding.etSurnameEditOfficeWorkerFragment.text.toString(),
                "id" to auth.currentUser!!.uid,
                "role" to "officeWorker",
                "access" to hashMapOf(
                    "checkTicket" to binding.switchCheckTicketsEditOfficeWorkerFragment.isChecked,
                    "class" to binding.switchClassEditOfficeWorkerFragment.isChecked,
                    "event" to binding.switchEventEditOfficeWorkerFragment.isChecked,
                    "sellTicket" to binding.switchSellTicketsEditOfficeWorkerFragment.isChecked,
                    "students" to binding.switchStudentsEditOfficeWorkerFragment.isChecked
                )
            )

            refOfficeWorker.setValue(temp)

            navControl.navigate(R.id.action_editOfficeWorkerFragment_to_changeOfficeWorkersFragment)

            Toast.makeText(context, getString(R.string.ToastEditOfficeWorker), Toast.LENGTH_SHORT).show()
        }
    }
}