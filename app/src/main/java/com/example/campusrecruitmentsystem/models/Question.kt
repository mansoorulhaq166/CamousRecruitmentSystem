package com.example.campusrecruitmentsystem.models

data class Question(
    var type: String = "", // e.g., "multiple_choice", "true_false", etc.
    var text: String = "",
    var options: List<String> = emptyList(),
    var correctOption: Int = -1

) {
    constructor() : this("", "", listOf(), -1)
}