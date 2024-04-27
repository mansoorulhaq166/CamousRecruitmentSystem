package com.example.campusrecruitmentsystem.models

data class MultipleChoiceQuestion(
    var question: String,
    var choiceA: String,
    var choiceB: String,
    var choiceC: String,
    var choiceD: String,
    var correctChoice: Int
)