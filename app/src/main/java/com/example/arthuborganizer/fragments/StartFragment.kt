package com.example.arthuborganizer.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.arthuborganizer.R
import com.example.arthuborganizer.model.ViewModelVariables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StartFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    private lateinit var navController : NavController
    private lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private val sharedViewModel: ViewModelVariables by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("users")

        Handler(Looper.myLooper()!!).postDelayed({
            if (auth.currentUser != null) {
                myRef.child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        when (snapshot.child("role").value) {
                            "officeWorker" -> {
                                navController.navigate(R.id.action_startFragment_to_officeWorkerMenuFragment)
                                sharedViewModel.role = "officeWorker"
                                sharedViewModel.idHouse = snapshot.child("id").value.toString()
                            }
                            "worker" -> {
                                navController.navigate(R.id.action_startFragment_to_workerMenuFragment)
                                sharedViewModel.role = "worker"
                                sharedViewModel.idHouse = snapshot.child("id").value.toString()
                            }
                            "admin" -> {
                                navController.navigate(R.id.action_startFragment_to_adminMenuFragment)
                                sharedViewModel.role = "admin"
                                sharedViewModel.idHouse = auth.currentUser!!.uid
                                sharedViewModel.checkTicket = true
                                sharedViewModel.clas = true
                                sharedViewModel.event = true
                                sharedViewModel.sellTicket = true
                                sharedViewModel.students = true
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, getString(R.string.ToastError), Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                navController.navigate(R.id.action_startFragment_to_loginFragment)
            }
        }, 2000)
    }

}