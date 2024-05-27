package com.example.campusrecruitmentsystem.models.recruiter

data class TestResult(
    var testId: String,
    var userId: String,
    var responses: MutableList<String>,
    var testName: String?,
    var testDuration: String?,
    var testDate: Long?
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", mutableListOf(), "", "", 0)
}