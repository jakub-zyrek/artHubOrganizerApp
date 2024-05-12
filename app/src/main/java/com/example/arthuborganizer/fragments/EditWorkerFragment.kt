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
import com.example.arthuborganizer.databinding.FragmentEditWorkerBinding
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditWorkerFragment : Fragment() {
    private lateinit var binding : FragmentEditWorkerBinding
    private val sharedViewModel: ViewModelVariables by activityViewModels()
    private lateinit var database : FirebaseDatabase
    private lateinit var refWorkers : DatabaseReference
    private lateinit var navControl : NavController
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditWorkerBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarEditWorker)

        binding.navBar.ivNavBarBack.setOnClickListener {
            navControl.navigate(R.id.action_editWorkerFragment_to_changeWorkersFragment)
        }

        binding.etEmailEditWorkerFragment.inputType = InputType.TYPE_NULL

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refWorkers = database.getReference("users").child(sharedViewModel.id)
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()

        refWorkers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.etEmailEditWorkerFragment.setText(snapshot.child("email").value.toString())
                binding.etNameEditWorkerFragment.setText(snapshot.child("name").value.toString())
                binding.etSurnameEditWorkerFragment.setText(snapshot.child("surname").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
            }

        })

        binding.btnAddEditWorkerFragment.setOnClickListener {
            val temp = hashMapOf(
                "email" to binding.etEmailEditWorkerFragment.text.toString(),
                "name" to binding.etNameEditWorkerFragment.text.toString(),
                "surname" to binding.etSurnameEditWorkerFragment.text.toString(),
                "id" to auth.currentUser!!.uid,
                "role" to "worker"
            )

            refWorkers.setValue(temp)

            navControl.navigate(R.id.action_editWorkerFragment_to_changeWorkersFragment)

            Toast.makeText(context, getString(R.string.ToastEditOfficeWorker), Toast.LENGTH_SHORT).show()
        }
    }
}