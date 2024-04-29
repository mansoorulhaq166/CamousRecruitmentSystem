package com.example.campusrecruitmentsystem.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.listeners.OnItemClickListener
import com.example.campusrecruitmentsystem.models.main.Job
import com.example.campusrecruitmentsystem.ui.recruiter.test.TestCreationActivity

class JobAdapter(
    private val jobs: List<Job>,
    private val fromTest: Boolean,
    private val context: Context
) : RecyclerView.Adapter<JobAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val job = jobs[position]

        holder.titleTextView.text = job.title
        holder.companyTextView.text = job.company
        holder.salaryTextView.text = job.salary
        holder.locationTextView.text = job.location
        holder.dateTextView.text = job.deadline

        holder.itemView.setOnClickListener {
            if (fromTest) {
                val intent = Intent(context, TestCreationActivity::class.java)
                intent.putExtra("jobId", job.id)
                intent.putExtra("jobTitle", job.title)
                context.startActivity(intent)
            } else {
                onItemClickListener?.onItemClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return jobs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewJobTitle)
        val companyTextView: TextView = itemView.findViewById(R.id.textViewCompany)
        val salaryTextView: TextView = itemView.findViewById(R.id.textViewSalary)
        val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewDeadlineDate)
    }
}