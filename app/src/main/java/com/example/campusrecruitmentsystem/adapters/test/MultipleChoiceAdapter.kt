package com.example.campusrecruitmentsystem.adapters.test

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.models.recruiter.MultipleChoiceQuestion


class MultipleChoiceAdapter(private val questions: MutableList<MultipleChoiceQuestion>) :
    RecyclerView.Adapter<MultipleChoiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionNumber: TextView = view.findViewById(R.id.tv_question_number)
        val questionText: EditText = view.findViewById(R.id.et_question)
        val questionChoice1: EditText = view.findViewById(R.id.choice_1)
        val questionChoice2: EditText = view.findViewById(R.id.choice_2)
        val questionChoice3: EditText = view.findViewById(R.id.choice_3)
        val questionChoice4: EditText = view.findViewById(R.id.choice_4)
        val checkbox1: CheckBox = view.findViewById(R.id.choice_1_checkbox)
        val checkbox2: CheckBox = view.findViewById(R.id.choice_2_checkbox)
        val checkbox3: CheckBox = view.findViewById(R.id.choice_3_checkbox)
        val checkbox4: CheckBox = view.findViewById(R.id.choice_4_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_multiple_choice, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questionNumber = "${position + 1}."
        holder.questionNumber.text = questionNumber
        val question = questions[position]
        holder.questionText.setHint(question.question)
        holder.questionChoice1.setText(question.choiceA)
        holder.questionChoice2.setText(question.choiceB)
        holder.questionChoice3.setText(question.choiceC)
        holder.questionChoice4.setText(question.choiceD)

        holder.questionText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                questions[position].question = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        holder.questionChoice1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                questions[position].choiceA = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        holder.questionChoice2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                questions[position].choiceB = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        holder.questionChoice3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                questions[position].choiceC = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        holder.questionChoice4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                questions[position].choiceD = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        when (question.correctChoice) {
            1 -> {
                holder.checkbox1.isChecked = true
            }

            2 -> {
                holder.checkbox2.isChecked = true
            }

            3 -> {
                holder.checkbox3.isChecked = true
            }

            4 -> {
                holder.checkbox4.isChecked = true
            }
        }

        holder.checkbox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                questions[position].correctChoice = 1
                holder.checkbox2.isChecked = false
                holder.checkbox3.isChecked = false
                holder.checkbox4.isChecked = false
            }
        }
        holder.checkbox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                questions[position].correctChoice = 2
                holder.checkbox1.isChecked = false
                holder.checkbox3.isChecked = false
                holder.checkbox4.isChecked = false
            }
        }
        holder.checkbox3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                questions[position].correctChoice = 3
                holder.checkbox1.isChecked = false
                holder.checkbox2.isChecked = false
                holder.checkbox4.isChecked = false
            }
        }
        holder.checkbox4.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                questions[position].correctChoice = 4
                holder.checkbox1.isChecked = false
                holder.checkbox2.isChecked = false
                holder.checkbox3.isChecked = false
            }
        }
    }

    override fun getItemCount(): Int = questions.size
    fun addQuestion(question: MultipleChoiceQuestion) {
        questions.add(question)
        notifyDataSetChanged()
    }

    fun getQuestions(): MutableList<MultipleChoiceQuestion> {
        return questions
    }
}