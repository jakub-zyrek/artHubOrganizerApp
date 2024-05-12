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
import com.example.arthuborganizer.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {
    private lateinit var binding : FragmentSignUpBinding
    private lateinit var navController : NavController
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var ref : DatabaseReference
    private lateinit var ref2 : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        ref = database.getReference("users")
        ref2 = database.getReference("")

        binding.btnSingInSignUpFragment.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        binding.btnSignUpSignUpFragment.setOnClickListener {
            val email = binding.etEmailSignUpFragment.text.toString().trim().lowercase()
            val password = binding.etPasswordSignUpFragment.text.toString().trim()
            val name = binding.etNameSignUpFragment.text.toString().trim()
            val surname = binding.etSurnameSignUpFragment.text.toString().trim()
            val repeatPassword = binding.etRepeatPasswordSignUpFragment.text.toString().trim()
            val nameOfCultureHouse = binding.etNameCultureHouseSignUpFragment.text.toString().trim()

            if (email.isEmpty() && password.isEmpty() && name.isEmpty() && surname.isEmpty() && repeatPassword.isEmpty() && nameOfCultureHouse.isEmpty()) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(context, getString(R.string.ToastPassword6CharactersError), Toast.LENGTH_SHORT).show()
            } else {
                if (password != repeatPassword) {
                    Toast.makeText(context, getString(R.string.ToastPasswordNotSame), Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = hashMapOf(
                                    "email" to email,
                                    "name" to name,
                                    "surname" to surname,
                                    "role" to "admin",
                                    "id" to auth.currentUser!!.uid
                                )

                                ref.child(auth.currentUser!!.uid).setValue(user)
                                ref2.child(auth.currentUser!!.uid).child("name").setValue(nameOfCultureHouse)

                                navController.navigate(R.id.action_signUpFragment_to_startFragment)
                            } else {
                                Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}