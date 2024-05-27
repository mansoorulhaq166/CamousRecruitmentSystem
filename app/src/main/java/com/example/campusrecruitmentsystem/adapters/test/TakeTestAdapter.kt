package com.example.campusrecruitmentsystem.adapters.test

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.databinding.ItemTakeTestBinding
import com.example.campusrecruitmentsystem.listeners.OnItemClickListener
import com.example.campusrecruitmentsystem.models.recruiter.Test
import com.example.campusrecruitmentsystem.ui.student.test.ShortTestActivity
import com.example.campusrecruitmentsystem.ui.student.test.ChoiceTestActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TakeTestAdapter(private val context: Context, private val testList: MutableList<Test>) :
    RecyclerView.Adapter<TakeTestAdapter.ViewHolder>() {
    private var itemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTakeTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTest = testList[position]
        holder.bind(currentTest)
    }

    override fun getItemCount(): Int {
        return testList.size
    }

    fun addData(test: Test) {
        testList.add(test)
        notifyItemInserted(testList.size - 1)
    }

    fun clearData() {
        testList.clear()
        notifyDataSetChanged()
    }

    fun addAllData(tests: List<Test>) {
        testList.addAll(tests)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemTakeTestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(test: Test) {
            val creationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date(test.creationTime!!)
            )
            val jobId = test.jobId

            binding.textTestName.text = test.testName
            binding.textTestTime.text = test.testTime + " seconds"
            binding.textTestType.text = test.testType

            val jobsRef = FirebaseDatabase.getInstance().getReference("jobs")
            val jobQuery = jobsRef.child(jobId.toString())
            jobQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val jobTitle = dataSnapshot.child("title").getValue(String::class.java)
                        val jobCompany = dataSnapshot.child("company").getValue(String::class.java)

                        // Set job details to appropriate TextViews
                        binding.textJobDetails.text = "Job Details: $jobTitle at $jobCompany"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Could Not Fetch Job Details", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            binding.btnStartTest.setOnClickListener {
                if (test.testType == "Multiple Choice") {
                    val intent = Intent(context, ChoiceTestActivity::class.java)
                    intent.putExtra("testId", test.testId)
                    binding.root.context.startActivity(intent)
                } else {
                    val intent = Intent(context, ShortTestActivity::class.java)
                    intent.putExtra("testId", test.testId)
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }
}