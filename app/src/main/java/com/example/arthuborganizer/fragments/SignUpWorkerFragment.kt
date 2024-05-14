package com.example.arthuborganizer.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.databinding.FragmentSignUpWorkerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUpWorkerFragment : Fragment() {
    private lateinit var binding : FragmentSignUpWorkerBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private lateinit var navControl : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpWorkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("users")
        navControl = Navigation.findNavController(view)
        var emailInDatabase = false

        binding.tvSignInSignUpWorkerFragment.setOnClickListener {
            navControl.navigate(R.id.action_signUpWorkerFragment_to_loginFragment)
        }

        binding.btnActivateSignUpWorkerFragment.setOnClickListener {
            if (binding.etLoginSignUpWorkerFragment.text.toString().isEmpty() or binding.etPasswordSignUpWorkerFragment.text.toString().isEmpty()) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else {
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            if (binding.etLoginSignUpWorkerFragment.text.toString().trim() == childSnapshot.child("email").value) {
                                emailInDatabase = true

                                val temp = childSnapshot.value as Map<*, *>

                                if (binding.etPasswordSignUpWorkerFragment.text.toString() == childSnapshot.child("password").value) {
                                    auth.createUserWithEmailAndPassword(childSnapshot.child("email").value.toString(), binding.etPasswordSignUpWorkerFragment.text.toString())
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                myRef.child(auth.currentUser!!.uid).setValue(temp)
                                                myRef.child(auth.currentUser!!.uid).child("password").removeValue()
                                                myRef.child(childSnapshot.key.toString()).removeValue()

                                                navControl.navigate(R.id.action_signUpWorkerFragment_to_startFragment)
                                                Toast.makeText(context, getString(R.string.ToastActiveAccount), Toast.LENGTH_SHORT).show()
                                            } else {
                                                if (it.exception is FirebaseAuthUserCollisionException) {
                                                    badPassword(childSnapshot, temp)
                                                }
                                            }
                                        }

                                    break
                                } else {
                                    badPassword(childSnapshot, temp)
                                    break
                                }
                            }
                        }

                        if (!emailInDatabase) {
                            Toast.makeText(context, getString(R.string.ToastEmailNotInDatabase), Toast.LENGTH_SHORT).show()
                        }

                        emailInDatabase = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }
    }

    private fun badPassword(childSnapshot : DataSnapshot, temp : Map<*, *>) {
        auth.signInWithEmailAndPassword(childSnapshot.child("email").value.toString(), binding.etPasswordSignUpWorkerFragment.text.toString())
            .addOnCompleteListener { it1 ->
                if (it1.isSuccessful) {
                    myRef.child(auth.currentUser!!.uid).setValue(temp)
                    myRef.child(auth.currentUser!!.uid).child("password").removeValue()
                    myRef.child(childSnapshot.key.toString()).removeValue()

                    navControl.navigate(R.id.action_signUpWorkerFragment_to_startFragment)
                } else {
                    Toast.makeText(context, getString(R.string.ToastResetPasswordActivateAccout), Toast.LENGTH_SHORT).show()
                    auth.sendPasswordResetEmail(binding.etLoginSignUpWorkerFragment.text.toString().trim())
                }
            }
    }
}