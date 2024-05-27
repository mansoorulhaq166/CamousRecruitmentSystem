package com.example.campusrecruitmentsystem.models.recruiter

data class Test(
    val testId: String? = "",
    val testName: String? = "",
    val testTime: String? = "",
    val creationTime: Long? = 0,
    val testType: String? = "",
    val userId: String? = "",
    val jobId: String? = "",
    val questions: List<Question>? = null
)

data class Question(
    val questionNumber: Int? = 0,
    val question: String? = "",
    val choiceA: String? = "",
    val choiceB: String? = "",
    val choiceC: String? = "",
    val choiceD: String? = "",
    val correctChoice: Int? = 0
)

data class TestTrueFalse(
    val testId: String? = "",
    val testName: String? = "",
    val testTime: String? = "",
    val creationTime: Long? = 0,
    val testType: String? = "",
    val recruiterId: String? = "",
    val jobId: String? = "",
    val questions: List<TrueFalseQuestion>? = null
)

data class TrueFalseQuestion(
    val questionNumber: Int? = 0,
    val question: String? = ""
)