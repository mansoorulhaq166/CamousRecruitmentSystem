package com.example.campusrecruitmentsystem.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.listeners.OnItemClickListener
import com.example.campusrecruitmentsystem.models.main.Job
import com.example.campusrecruitmentsystem.ui.recruiter.test.TestCreationActivity
import com.example.campusrecruitmentsystem.ui.student.jobs.AppliedJobsDetailActivity
import com.google.firebase.database.FirebaseDatabase

class JobAdapter(
    private var jobs: List<Job>,
    private val fromTest: Boolean,
    private val fromMain: Boolean,
    private val studentApplied: Boolean,
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
        if (fromMain) {
            holder.editJob.visibility = View.VISIBLE
        }

        holder.editJob.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.delete_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.menu_delete) {
                    showDeleteConfirmationDialog(job)
                    true
                } else {
                    false
                }
            }
            popupMenu.show()
        }

        holder.itemView.setOnClickListener {
            if (fromTest) {
                val intent = Intent(context, TestCreationActivity::class.java)
                intent.putExtra("jobId", job.id)
                intent.putExtra("jobTitle", job.title)
                context.startActivity(intent)
            } else if (studentApplied) {
                val intent = Intent(context, AppliedJobsDetailActivity::class.java)
                intent.putExtra("jobId", job.id)
                context.startActivity(intent)
            } else {
                onItemClickListener?.onItemClick(position)
            }
        }
    }

    private fun deleteJob(jobId: String) {
        val jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
        jobRef.removeValue()
    }

    private fun showDeleteConfirmationDialog(job: Job) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete this job?")

        builder.setPositiveButton("Yes") { _, _ ->
            deleteJob(job.id)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
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
        val editJob: ImageView = itemView.findViewById(R.id.item_job_edit)
    }
}