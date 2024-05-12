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
import com.example.arthuborganizer.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var binding : FragmentLoginBinding
    private lateinit var navController : NavController
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)

        binding.tvCreatingAdminAccountLoginFragment.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.tvActivateAccountLoginFragment.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_signUpWorkerFragment)
        }

        binding.btnSignInLoginFragment.setOnClickListener {
            val email = binding.etLoginLoginFragment.text.toString().trim()
            val password = binding.etPasswordLoginFragment.text.toString().trim()

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(context, getString(R.string.ToastFillDetails), Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            navController.navigate(R.id.action_loginFragment_to_startFragment)
                        } else {
                            Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnCanceledListener {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}