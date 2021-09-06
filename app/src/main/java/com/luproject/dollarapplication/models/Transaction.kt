package com.luproject.dollarapplication.models

import java.util.*

data class Transaction(val from:String,val lat:Double,val lng:Double,val imagePaths:List<String>,val date:String,val signaturePath:String,val amount:Double,val currency:String,val serials:ArrayList<String>)