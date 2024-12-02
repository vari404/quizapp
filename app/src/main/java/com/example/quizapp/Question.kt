package com.example.quizapp

data class Question(
    val questionText: String,
    val options: ArrayList<String>,
    val correctAnswerIndex: Int
)
