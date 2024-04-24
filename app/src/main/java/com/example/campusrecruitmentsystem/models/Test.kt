package com.example.campusrecruitmentsystem.models

data class Test(
    var title: String = "",
    var description: String = "",
    var timeLimit: Long = 0, // in minutes
    var questions: List<Question> = emptyList()
)