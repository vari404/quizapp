package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.io.IOException

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvTimer: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var radioGroupOptions: RadioGroup
    private lateinit var buttonNext: Button

    private var questionList: ArrayList<Question> = arrayListOf()
    private var currentQuestionIndex: Int = 0
    private var score: Int = 0
    private var totalQuestions: Int = 10
    private var timeLeftInMillis: Long = 30000 // 30 seconds for each question
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Retrieve user selections
        val numberOfQuestions = intent.getStringExtra("numberOfQuestions") ?: "10"
        val categoryId = intent.getIntExtra("categoryId", -1)
        val difficulty = intent.getStringExtra("difficulty") ?: "any"
        val type = intent.getStringExtra("type") ?: "any"

        // Initialize UI components
        tvQuestionNumber = findViewById(R.id.tv_question_number)
        tvScore = findViewById(R.id.tv_score)
        tvQuestion = findViewById(R.id.tv_question)
        tvTimer = findViewById(R.id.tv_timer)
        progressBar = findViewById(R.id.progress_bar)
        radioGroupOptions = findViewById(R.id.radio_group_options)
        buttonNext = findViewById(R.id.button_next)

        // Fetch questions based on selections
        fetchQuestions(numberOfQuestions, categoryId, difficulty, type)

        // Set click listener for Next button
        buttonNext.setOnClickListener {
            checkAnswer()
        }
    }

    private fun fetchQuestions(
        numberOfQuestions: String,
        categoryId: Int,
        difficulty: String,
        type: String
    ) {
        val client = OkHttpClient()

        // Use the updated toHttpUrl() extension function
        val urlBuilder = "https://opentdb.com/api.php".toHttpUrl().newBuilder()
        urlBuilder.addQueryParameter("amount", numberOfQuestions)

        if (categoryId != -1) {
            urlBuilder.addQueryParameter("category", categoryId.toString())
        }

        if (difficulty != "any") {
            urlBuilder.addQueryParameter("difficulty", difficulty)
        }

        if (type != "any") {
            val typeParam = when (type) {
                "multiple choice" -> "multiple"
                "true/false" -> "boolean"
                else -> "any"
            }
            urlBuilder.addQueryParameter("type", typeParam)
        }

        val url = urlBuilder.build().toString()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@QuizActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonData = responseBody.string()
                    val jsonObject = JSONObject(jsonData)
                    val responseCode = jsonObject.getInt("response_code")

                    if (responseCode == 0) {
                        val resultsArray = jsonObject.getJSONArray("results")

                        for (i in 0 until resultsArray.length()) {
                            val questionObject = resultsArray.getJSONObject(i)
                            val questionText = Html.fromHtml(questionObject.getString("question")).toString()

                            val correctAnswer = Html.fromHtml(questionObject.getString("correct_answer")).toString()
                            val incorrectAnswers = questionObject.getJSONArray("incorrect_answers")
                            val options = ArrayList<String>()

                            for (j in 0 until incorrectAnswers.length()) {
                                options.add(Html.fromHtml(incorrectAnswers.getString(j)).toString())
                            }

                            options.add(correctAnswer)
                            options.shuffle()

                            val correctAnswerIndex = options.indexOf(correctAnswer)

                            questionList.add(
                                Question(
                                    questionText,
                                    options,
                                    correctAnswerIndex
                                )
                            )
                        }

                        runOnUiThread {
                            showQuestion()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@QuizActivity, "No questions found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun showQuestion() {
        if (currentQuestionIndex < questionList.size) {
            val currentQuestion = questionList[currentQuestionIndex]

            tvQuestionNumber.text = "Question ${currentQuestionIndex + 1}"
            tvScore.text = "Score: $score"
            tvQuestion.text = currentQuestion.questionText

            // Clear previous selection
            radioGroupOptions.removeAllViews()

            // Dynamically add radio buttons based on the number of options
            for (option in currentQuestion.options) {
                val radioButton = RadioButton(this)
                radioButton.text = option
                radioGroupOptions.addView(radioButton)
            }

            // Reset and start the timer
            resetTimer()
        } else {
            // No more questions, finish the quiz
            finishQuiz()
        }
    }

    private fun checkAnswer() {
        val selectedOptionId = radioGroupOptions.checkedRadioButtonId

        if (selectedOptionId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedOptionId)
            val selectedAnswerIndex = radioGroupOptions.indexOfChild(selectedRadioButton)

            val currentQuestion = questionList[currentQuestionIndex]

            if (selectedAnswerIndex == currentQuestion.correctAnswerIndex) {
                score++
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            } else {
                val correctAnswer = currentQuestion.options[currentQuestion.correctAnswerIndex]
                Toast.makeText(this, "Incorrect! Correct Answer: $correctAnswer", Toast.LENGTH_SHORT).show()
            }

            currentQuestionIndex++
            showQuestion()
        } else {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetTimer() {
        // Cancel any existing timer
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        timeLeftInMillis = 30000 // 30 seconds
        tvTimer.text = "Time: 30"
        progressBar.max = 30
        progressBar.progress = 30

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val secondsLeft = (timeLeftInMillis / 1000).toInt()
                tvTimer.text = "Time: $secondsLeft"
                progressBar.progress = secondsLeft
            }

            override fun onFinish() {
                Toast.makeText(this@QuizActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                currentQuestionIndex++
                showQuestion()
            }
        }.start()
    }

    private fun finishQuiz() {
        // Cancel the timer if it's still running
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        // Navigate to SummaryActivity
        val intent = Intent(this, SummaryActivity::class.java)
        intent.putExtra("score", score)
        intent.putExtra("totalQuestions", questionList.size)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer to avoid memory leaks
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
