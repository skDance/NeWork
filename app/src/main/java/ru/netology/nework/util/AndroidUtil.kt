package ru.netology.nework.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.Date

object AndroidUtil {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun formatDate(date: String): String {
        val dateString = "2022-07-13T07:58:57.835201Z"
        val currentFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        val date: Date = currentFormat.parse(dateString)
        val targetFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return targetFormat.format(date)
    }
}