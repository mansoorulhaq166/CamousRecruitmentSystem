package com.example.campusrecruitmentsystem.models.main

data class ApplicationDetails(
    val applicationId: String? = null,
    val jobId: String? = null,
    val studentId: String? = null,
    val studentName: String? = null,
    val studentEmail: String? = null,
    val studentPhone: String? = null,
    val recruiterId: String? = null,
    val resumeUrl: String? = null,
    val applicationDate: String? = null,
    val status: String? = null,
    val studentLocation: String? = null,
    val relevantExperience: String? = null,
    val comments: String? = null,
    val notes: String? = null,
    val offerLetterSent:Boolean? = false,
    val offerLetterUrl:String? = null
)