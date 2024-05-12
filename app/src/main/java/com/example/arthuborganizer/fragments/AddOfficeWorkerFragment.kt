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
import com.example.arthuborganizer.databinding.FragmentAddOfficeWorkerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddOfficeWorkerFragment : Fragment() {
    private lateinit var binding : FragmentAddOfficeWorkerBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var refUsers : DatabaseReference
    private lateinit var navControl : NavController
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddOfficeWorkerBinding.inflate(inflater, container, false)

        binding.navBar.tvNavBarLabel.text = getString(R.string.navBarAddOfficeWorker)

        binding.navBar.ivNavBarBack.setOnClickListener {
            back()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        refUsers = database.getReference("users")
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()

        binding.btnAddAddOfficeWorkerFragment.setOnClickListener {
            if (
                binding.etPasswordAddOfficeWorkerFragment.text.toString().isEmpty() or
                binding.etEmailAddOfficeWorkerFragment.text.toString().isEmpty() or
                binding.etNameAddOfficeWorkerFragment.text.toString().isEmpty() or
                binding.etSurnameAddOfficeWorkerFragment.text.toString().isEmpty() or
                binding.etRepeatPasswordAddOfficeWorkerFragment.text.toString().isEmpty()
            ) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else if (binding.etPasswordAddOfficeWorkerFragment.text.toString() != binding.etRepeatPasswordAddOfficeWorkerFragment.text.toString()) {
                Toast.makeText(context, getString(R.string.ToastPasswordNotSame), Toast.LENGTH_SHORT).show()
            } else {
                var emails = true

                refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (x in snapshot.children) {
                            if (binding.etEmailAddOfficeWorkerFragment.text.toString().trim() == x.child("email").value.toString()) {
                                emails = false
                                break
                            }
                        }

                        if (emails) {
                            val user = hashMapOf(
                                "access" to hashMapOf(
                                    "checkTicket" to binding.switchCheckTicketsAddOfficeWorkerFragment.isChecked,
                                    "class" to binding.switchClassAddOfficeWorkerFragment.isChecked,
                                    "event" to binding.switchEventAddOfficeWorkerFragment.isChecked,
                                    "sellTicket" to binding.switchSellTicketAddOfficeWorkerFragment.isChecked,
                                    "students" to binding.switchStudentsAddOfficeWorkerFragment.isChecked
                                ),
                                "email" to binding.etEmailAddOfficeWorkerFragment.text.toString().trim(),
                                "name" to binding.etNameAddOfficeWorkerFragment.text.toString().trim(),
                                "surname" to binding.etSurnameAddOfficeWorkerFragment.text.toString().trim(),
                                "role" to "officeWorker",
                                "password" to binding.etPasswordAddOfficeWorkerFragment.text.toString(),
                                "id" to auth.currentUser!!.uid
                            )

                            refUsers.push().setValue(user)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(context, getString(R.string.ToastAddWorker), Toast.LENGTH_SHORT).show()
                                        back()
                                    } else {
                                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, getString(R.string.ToastEmailInDatabase), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }
    }

    private fun back() {
        navControl.navigate(R.id.action_addOfficeWorkerFragment_to_adminMenuFragment)
    }
}