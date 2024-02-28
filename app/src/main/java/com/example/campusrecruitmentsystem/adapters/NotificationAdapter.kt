package com.example.campusrecruitmentsystem.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrecruitmentsystem.R
import com.example.campusrecruitmentsystem.ui.recruiter.AppliedApplicationsActivity

class NotificationAdapter(private val notifications: List<String>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNotification: TextView = itemView.findViewById(R.id.textViewNotification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.textViewNotification.text = notification

        val context = holder.itemView.context
        holder.textViewNotification.setOnClickListener {
            context.startActivity(Intent(context, AppliedApplicationsActivity::class.java))
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}