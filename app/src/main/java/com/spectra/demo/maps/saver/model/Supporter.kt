package com.spectra.demo.maps.saver.model

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue

val supporter = Supporter.getInstance()

class Supporter {

    companion object {

        @Volatile
        private var INSTANCE: Supporter? = null

        fun getInstance(): Supporter =
            INSTANCE ?: synchronized(this) {
                val instance = Supporter()
                INSTANCE = instance
                instance
            }

    }


    fun getColor(resId: Int, mContext: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray = mContext.obtainStyledAttributes(typedValue.data, intArrayOf(resId))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }


}