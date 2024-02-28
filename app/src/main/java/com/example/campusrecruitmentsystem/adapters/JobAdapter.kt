package com.example.campusrecruitmentsystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.listeners.OnItemClickListener
import com.example.campusrecruitmentsystem.models.Job

class JobAdapter(private val jobs: List<Job>) : RecyclerView.Adapter<JobAdapter.ViewHolder>() {

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
        holder.descriptionTextView.text = job.description
        holder.salaryTextView.text = job.salary
        holder.locationTextView.text = job.location
        holder.criteriaTextView.text = job.criteria

    }

    override fun getItemCount(): Int {
        return jobs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewJobTitle)
        val companyTextView: TextView = itemView.findViewById(R.id.textViewCompany)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val salaryTextView: TextView = itemView.findViewById(R.id.textViewSalary)
        val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)
        val criteriaTextView: TextView = itemView.findViewById(R.id.textViewCriteria)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(position)
                }
            }
        }
    }
}