package com.example.arthuborganizer.model

import androidx.lifecycle.ViewModel
import java.util.Calendar

class ViewModelVariables : ViewModel() {
    //variables to using app
    var id:String = ""
    var idClass:String = ""
    var selectedDate: Calendar = Calendar.getInstance()

    //variables for users
    var role:String = ""
    var event:Boolean = false
    var checkTicket:Boolean = false
    var clas:Boolean = false
    var sellTicket:Boolean = false
    var students:Boolean = false
    var idHouse: String = ""

    //variables to class
    var typeOfClass = "change"
}