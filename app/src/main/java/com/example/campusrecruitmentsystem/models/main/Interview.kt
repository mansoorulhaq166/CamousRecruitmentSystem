package com.example.campusrecruitmentsystem.models.main

data class Interview(
    val interviewId: String? = null,
    val studentId: String? = null,
    val applicationId: String? = null,
    val interviewDateTime: String? = null,
    val interviewType: String? = null,
    val interviewerName: String? = null,
    val interviewerEmail: String? = null,
    val additionalInformation: String? = null,
    val jobId: String? = null,
    val recruiterId: String? = null,
    val location: String? = null,
    val callType: String? = null,
    val callMedium: String? = null,
    val callJoinDetails: String? = null,
    val invitationStatus: String? = null
)