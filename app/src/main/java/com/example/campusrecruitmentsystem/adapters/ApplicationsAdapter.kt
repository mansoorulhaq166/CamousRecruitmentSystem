package com.example.campusrecruitmentsystem.adapters

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.models.ApplicationDetails
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ApplicationsAdapter(private val appliedApplications: List<ApplicationDetails>) :
    RecyclerView.Adapter<ApplicationsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewStudentName: TextView = itemView.findViewById(R.id.textViewStudentName)
        val textViewJobTitle: TextView = itemView.findViewById(R.id.textViewJobTitle)
        val textViewApplicationDate: TextView = itemView.findViewById(R.id.textViewApplicationDate)
        val btnDownloadResume: TextView = itemView.findViewById(R.id.btnDownloadResume)
        val progressBar: CircularProgressIndicator = itemView.findViewById(R.id.progress_bar)
        val progressText: TextView = itemView.findViewById(R.id.progress_text)
        var userId: String? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_item_applied_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appliedApplication = appliedApplications[position]
        val appContext = holder.itemView.context
        var name = ""
        var title = ""

        holder.userId = appliedApplication.studentId!!
        fetchStudentName(appliedApplication.studentId) { studentName ->
            holder.textViewStudentName.text = studentName
            name = studentName
        }

        fetchJobTitle(appliedApplication.jobId!!) { jobTitle ->
            holder.textViewJobTitle.text = jobTitle
            title = jobTitle
        }


        holder.textViewApplicationDate.text = appliedApplication.applicationDate

        holder.btnDownloadResume.setOnClickListener {
            val resumeUrl = appliedApplication.resumeUrl

            if (resumeUrl != null) {
                if (resumeUrl.isNotEmpty()) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        downloadResume(appContext, resumeUrl, name, title, holder)
                    } else {
                        Toast.makeText(appContext, "User not authenticated", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(appContext, "Resume URL is empty", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return appliedApplications.size
    }

    private fun fetchStudentName(studentId: String, callback: (String) -> Unit) {
        FirebaseDatabase.getInstance().getReference("users").child(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val studentName = snapshot.child("name").getValue(String::class.java)
                        callback(studentName.orEmpty())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationsAdapter", "Error fetching student name: ${error.message}")
                }
            })
    }

    private fun fetchJobTitle(jobId: String, callback: (String) -> Unit) {
        FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val jobTitle = snapshot.child("title").getValue(String::class.java)
                        callback(jobTitle.orEmpty())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationsAdapter", "Error fetching job title: ${error.message}")
                }
            })
    }


    private fun downloadResume(
        context: Context,
        resumeUrl: String,
        studentName: String,
        jobTitle: String,
        holder: ViewHolder
    ) {
        holder.progressBar.visibility = View.VISIBLE
        holder.progressText.visibility = View.VISIBLE
        holder.btnDownloadResume.visibility = View.GONE

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(resumeUrl)

        val fileName = "$studentName - $jobTitle - resume.pdf"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        try {
//            val outputStream = FileOutputStream(file)
            storageRef.getFile(file)
                .addOnSuccessListener {
                    Toast.makeText(context, "File downloaded successfully", Toast.LENGTH_SHORT)
                        .show()
                    holder.progressBar.visibility = View.GONE
                    holder.progressText.visibility = View.GONE
                    holder.btnDownloadResume.visibility = View.VISIBLE
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to download resume", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                    holder.progressBar.visibility = View.GONE
                    holder.progressText.visibility = View.GONE
                    holder.btnDownloadResume.visibility = View.VISIBLE
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                    holder.progressText.text = context.getString(R.string.upload_progress, progress)
                    holder.progressBar.progress = progress
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to download resume", Toast.LENGTH_SHORT).show()
            holder.progressBar.visibility = View.GONE
            holder.btnDownloadResume.visibility = View.VISIBLE
        }
    }
}