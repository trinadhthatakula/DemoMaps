package com.spectra.demo.maps.saver.model

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.util.TypedValue
import com.google.android.gms.maps.model.LatLng
import org.koin.core.annotation.Single
import kotlin.collections.ArrayList


@Single
class Supporter {

    var polyData: PolyData = PolyData()
    val markedPoints: ArrayList<LatLng> = ArrayList()
    var mapSnap: Bitmap? = null
    var polyMode = PolyMode.GON

    fun getColor(resId: Int, mContext: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray = mContext.obtainStyledAttributes(typedValue.data, intArrayOf(resId))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }


}