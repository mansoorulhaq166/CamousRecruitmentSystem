package com.example.campusrecruitmentsystem.adapters

import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.models.Question

class QuestionAdapter(private val questions: List<Question>) :
    RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editTextQuestion: EditText = itemView.findViewById(R.id.editTextQuestion)
        val spinnerQuestionType: Spinner = itemView.findViewById(R.id.spinnerQuestionType)
        val layoutOptions: LinearLayout = itemView.findViewById(R.id.layoutOptions)
        val spinnerCorrectOption: Spinner = itemView.findViewById(R.id.spinnerCorrectOption)
        val btnAddOption: TextView = itemView.findViewById(R.id.btnAddOption)
        val btnRemoveOption: TextView = itemView.findViewById(R.id.btnRemoveOption)

        init {
            val questionTypes = arrayOf("Single Choice", "Multiple Choice", "Text Answer")
            val questionTypeAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, questionTypes)
            spinnerQuestionType.adapter = questionTypeAdapter

            btnAddOption.setOnClickListener {
                addOption()
            }

            btnRemoveOption.setOnClickListener {
                // Remove the last option when the "Remove Option" button is clicked
                removeOption()
            }
        }

        private fun addOption() {
            val optionLayout = LinearLayout(itemView.context)
            optionLayout.orientation = LinearLayout.HORIZONTAL

            val newOptionEditText = EditText(itemView.context)
            newOptionEditText.hint = "Enter option"
            layoutOptions.addView(newOptionEditText)

            val correctOptionSpinner = Spinner(itemView.context)
            val options = arrayOf("A", "B", "C", "D") // Customize as needed
            val correctOptionAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, options)
            correctOptionSpinner.adapter = correctOptionAdapter
            optionLayout.addView(correctOptionSpinner)

            // Add the option layout to the main layoutOptions
            layoutOptions.addView(optionLayout)
        }

        // Function to remove the last option dynamically
        private fun removeOption() {
            if (layoutOptions.childCount > 0) {
                layoutOptions.removeViewAt(layoutOptions.childCount - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentQuestion = questions[position]

        val questionText = Editable.Factory.getInstance().newEditable(currentQuestion.text)
        holder.editTextQuestion.text = questionText

        val questionTypes = arrayOf("Single Choice", "Multiple Choice", "Text Answer")
        val questionTypePosition = questionTypes.indexOf(currentQuestion.type)
        holder.spinnerQuestionType.setSelection(questionTypePosition)

        holder.layoutOptions.removeAllViews()

        for (option in currentQuestion.options) {
            val optionLayout = LinearLayout(holder.itemView.context)
            optionLayout.orientation = LinearLayout.HORIZONTAL

            // Create EditText for the option
            val optionEditText = EditText(holder.itemView.context)
            optionEditText.text = Editable.Factory.getInstance().newEditable(option)
            optionLayout.addView(optionEditText)

            // Create Spinner for correct option
            val correctOptionSpinner = Spinner(holder.itemView.context)
            val options = arrayOf("A", "B", "C", "D") // Customize as needed
            val correctOptionAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, options)
            correctOptionSpinner.adapter = correctOptionAdapter
            optionLayout.addView(correctOptionSpinner)

            // Add the option layout to the main layoutOptions
            holder.layoutOptions.addView(optionLayout)

        }
    }

    override fun getItemCount(): Int {
        return questions.size
    }
}