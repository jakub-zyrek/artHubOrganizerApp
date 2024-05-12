package com.example.arthuborganizer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentAddWorkerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddWorkerFragment : Fragment() {
    private lateinit var binding : FragmentAddWorkerBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var refWorkers : DatabaseReference
    private lateinit var navControl : NavController
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddWorkerBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddWorker)

        binding.navBar.ivNavBarBack.setOnClickListener { back() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        refWorkers = database.getReference("users")
        navControl = Navigation.findNavController(view)

        binding.btnAddAddWorkerFragment.setOnClickListener {
            if (
                binding.etPasswordAddWorkerFragment.text.toString().isEmpty() ||
                binding.etEmailAddWorkerFragment.text.toString().isEmpty() ||
                binding.etNameAddWorkerFragment.text.toString().isEmpty() ||
                binding.etSurnameAddWorkerFragment.text.toString().isEmpty() ||
                binding.etRepeatPasswordAddWorkerFragment.text.toString().isEmpty()
            ) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else if (binding.etPasswordAddWorkerFragment.text.toString() != binding.etRepeatPasswordAddWorkerFragment.text.toString()) {
                Toast.makeText(context, getString(R.string.ToastPasswordNotSame), Toast.LENGTH_SHORT).show()
            } else {
                var emails = true

                refWorkers.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (x in snapshot.children) {
                            if (binding.etEmailAddWorkerFragment.text.toString().trim() == x.child("email").value.toString()) {
                                emails = false
                                break
                            }
                        }

                        if (emails) {
                            val user = hashMapOf(
                                "email" to binding.etEmailAddWorkerFragment.text.toString().trim(),
                                "name" to binding.etNameAddWorkerFragment.text.toString().trim(),
                                "surname" to binding.etSurnameAddWorkerFragment.text.toString().trim(),
                                "role" to "worker",
                                "password" to binding.etPasswordAddWorkerFragment.text.toString(),
                                "id" to auth.currentUser!!.uid
                            )

                            refWorkers.push().setValue(user)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(context,getString(R.string.ToastAddWorker), Toast.LENGTH_SHORT).show()
                                        back()
                                    } else Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                                }
                        } else Toast.makeText(context, getString(R.string.ToastEmailInDatabase), Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    private fun back() {
        navControl.navigate(R.id.action_addWorkerFragment_to_adminMenuFragment)
    }
}