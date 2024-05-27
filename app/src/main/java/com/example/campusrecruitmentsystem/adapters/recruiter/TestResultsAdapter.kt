package com.example.campusrecruitmentsystem.adapters.recruiter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.models.recruiter.TestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestResultsAdapter(
    private val results: List<TestResult>,
    private val onItemClick: (TestResult) -> Unit
) :
    RecyclerView.Adapter<TestResultsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_result_test, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.bind(result)
    }

    override fun getItemCount() = results.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(result: TestResult) {
            val creationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date(result.testDate!!)
            )

            itemView.setOnClickListener { onItemClick(result) }
            itemView.findViewById<TextView>(R.id.textTestName).text = result.testName
            itemView.findViewById<TextView>(R.id.textTestCreationDate).text = creationTime
            itemView.findViewById<TextView>(R.id.textTestTime).text = result.testDuration + " seconds"
        }
    }
}