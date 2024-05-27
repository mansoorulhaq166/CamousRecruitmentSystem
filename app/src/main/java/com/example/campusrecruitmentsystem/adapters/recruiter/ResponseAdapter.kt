package com.example.campusrecruitmentsystem.adapters.recruiter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R

class ResponseAdapter(private val responses: List<String>) :
    RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_response, parent, false)
        return ResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        val response = responses[position]
        holder.bind(position, response)
    }

    override fun getItemCount() = responses.size

    class ResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvResponse: TextView = itemView.findViewById(R.id.tv_response)

        fun bind(position: Int, response: String) {
            tvResponse.text = "Question ${position + 1}: $response"
        }
    }
}