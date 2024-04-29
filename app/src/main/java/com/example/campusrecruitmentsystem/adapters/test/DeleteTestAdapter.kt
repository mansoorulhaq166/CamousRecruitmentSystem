package com.example.campusrecruitmentsystem.adapters.test

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.databinding.ItemDeleteTestBinding
import com.example.campusrecruitmentsystem.listeners.TestItemClickListener
import com.example.campusrecruitmentsystem.models.recruiter.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeleteTestAdapter(private val testList: MutableList<Test>) :
    RecyclerView.Adapter<DeleteTestAdapter.ViewHolder>() {
    private var itemClickListener: TestItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDeleteTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun setOnItemClickListener(listener: TestItemClickListener) {
        itemClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemDeleteTestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(test: Test) {
            val creationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date(test.creationTime)
            )

            binding.textTestName.text = test.testName
            binding.textTestCreationDate.text = creationTime
            binding.textTestTime.text = test.testTime + " seconds"

            binding.btnTestDelete.setOnClickListener {
                itemClickListener?.onDeleteClicked(adapterPosition, test.testId, test.testType)
                testList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                notifyDataSetChanged()
            }
        }
    }
}