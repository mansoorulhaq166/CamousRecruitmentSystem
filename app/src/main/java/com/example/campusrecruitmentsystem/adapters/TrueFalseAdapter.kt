package com.example.campusrecruitmentsystem.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R

class TrueFalseAdapter(private val questions: MutableList<String>) :
    RecyclerView.Adapter<TrueFalseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionNumber: TextView = view.findViewById(R.id.tv_question_number)
        val questionText: EditText = view.findViewById(R.id.et_question)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_true_false, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questionNumber = "${position + 1}."
        holder.questionNumber.text = questionNumber
        holder.questionText.setText(questions[position])
        holder.questionText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                questions[position] = holder.questionText.text.toString()
            }
        }
    }

    override fun getItemCount(): Int = questions.size

    fun addQuestion(question: String) {
        questions.add(question)
        notifyDataSetChanged()
    }

    fun getQuestions(): MutableList<String> {
        return questions
    }
}