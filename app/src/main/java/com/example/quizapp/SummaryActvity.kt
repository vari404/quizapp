package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    private lateinit var tvScoreSummary: TextView
    private lateinit var buttonRestart: Button
    private lateinit var buttonExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        tvScoreSummary = findViewById(R.id.tv_score_summary)
        buttonRestart = findViewById(R.id.button_restart)
        buttonExit = findViewById(R.id.button_exit)

        // Get the score from the intent
        val score = intent.getIntExtra("score", 0)
        val totalQuestions = intent.getIntExtra("totalQuestions", 0)

        tvScoreSummary.text = "You scored $score out of $totalQuestions"

        buttonRestart.setOnClickListener {
            // Restart the quiz by going back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonExit.setOnClickListener {
            // Exit the app
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        // Prevent user from going back to the quiz after finishing
        Toast.makeText(this, "Please use the buttons to navigate", Toast.LENGTH_SHORT).show()
    }
}
