package com.example.campusrecruitmentsystem

import android.content.Context
import android.widget.EditText
import android.widget.Toast

object Utils {
    fun isFieldEmpty(context: Context, editText: EditText, fieldName: String): Boolean {
        val fieldValue = editText.text.toString().trim()
        if (fieldValue.isEmpty()) {
            editText.requestFocus()
            Toast.makeText(context, "Please fill in $fieldName", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }
}