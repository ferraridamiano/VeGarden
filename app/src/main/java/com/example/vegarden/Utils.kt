package com.example.vegarden
import android.util.Patterns

fun String?.isValidEmail() : Boolean = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun String?.isValidPassword() : Boolean = !isNullOrEmpty() && this.length >= 8

val mapVegetableResource = hashMapOf<Int, Int>(
    1 to R.drawable.veg_broccoli,
    2 to R.drawable.veg_carrots,
    3 to R.drawable.veg_corn,
    4 to R.drawable.veg_cucumbers,
    5 to R.drawable.veg_eggplants,
    6 to R.drawable.veg_leeks,
    7 to R.drawable.veg_melons,
    8 to R.drawable.veg_peas,
    9 to R.drawable.veg_peppers,
    10 to R.drawable.veg_radishes,
    11 to R.drawable.veg_tomatoes,
    12 to R.drawable.veg_watermelons,
    9999 to R.drawable.veg_other,
)