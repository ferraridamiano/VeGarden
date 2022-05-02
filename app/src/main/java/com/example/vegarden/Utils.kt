package com.example.vegarden

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Patterns
import androidx.core.content.res.ResourcesCompat

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

fun String?.isValidEmail(): Boolean =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String?.isValidPassword(): Boolean = !isNullOrEmpty() && this.length >= 8

fun getPlotDrawable(context: Context, cropID: Int): Drawable {
    return when (cropID) {
        0 -> {
            ResourcesCompat.getDrawable(context.resources, R.drawable.plot_uncultivated, null)!!
        }
        in 1..13 -> {
            val layerDrawable = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.plot_vegetables,
                null
            ) as LayerDrawable
            if (cropID == 13) { // User clicks on "others"
                layerDrawable.setDrawableByLayerId(
                    R.id.vegetableImage, ResourcesCompat.getDrawable(
                        context.resources,
                        mapVegetableResource[9999]!!,
                        null
                    )
                )
            } else {
                layerDrawable.setDrawableByLayerId(
                    R.id.vegetableImage, ResourcesCompat.getDrawable(
                        context.resources,
                        mapVegetableResource[cropID]!!,
                        null
                    )
                )
            }
            layerDrawable
        }
        else -> {
            ResourcesCompat.getDrawable(context.resources, R.drawable.plot_uncultivated, null)!!
        }
    }
}