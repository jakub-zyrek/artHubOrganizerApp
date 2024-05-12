package com.example.arthuborganizer.model

import androidx.lifecycle.ViewModel

class ViewModelVariables : ViewModel() {
    //variables to using app
    var id:String = ""

    //variables for users
    var role:String = ""
    var event:Boolean = false
    var checkTicket:Boolean = false
    var clas:Boolean = false
    var sellTicket:Boolean = false
    var students:Boolean = false
    var idHouse: String = ""
}