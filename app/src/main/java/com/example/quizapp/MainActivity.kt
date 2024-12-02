package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerNumberOfQuestions: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var buttonStartQuiz: Button
    private lateinit var progressBar: ProgressBar

    private val categoryList = ArrayList<String>()
    private val categoryMap = HashMap<String, Int>() // Map category name to ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerNumberOfQuestions = findViewById(R.id.spinner_number_of_questions)
        spinnerCategory = findViewById(R.id.spinner_category)
        spinnerDifficulty = findViewById(R.id.spinner_difficulty)
        spinnerType = findViewById(R.id.spinner_type)
        buttonStartQuiz = findViewById(R.id.button_start_quiz)
        progressBar = findViewById(R.id.progress_bar)

        setupNumberOfQuestionsSpinner()
        setupDifficultySpinner()
        setupTypeSpinner()

        // Initially hide the category spinner and progress bar
        spinnerCategory.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        // Fetch categories from the API
        fetchCategories()

        buttonStartQuiz.setOnClickListener {
            startQuiz()
        }
    }

    private fun setupNumberOfQuestionsSpinner() {
        val numberOfQuestionsOptions = listOf("5", "10", "15")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, numberOfQuestionsOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNumberOfQuestions.adapter = adapter
    }

    private fun setupDifficultySpinner() {
        val difficultyOptions = listOf("Any", "Easy", "Medium", "Hard")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = adapter
    }

    private fun setupTypeSpinner() {
        val typeOptions = listOf("Any", "Multiple Choice", "True/False")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
    }

    private fun fetchCategories() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://opentdb.com/api_category.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonData = responseBody.string()
                    val jsonObject = JSONObject(jsonData)
                    val categoriesArray = jsonObject.getJSONArray("trivia_categories")

                    categoryList.add("Any")
                    categoryMap["Any"] = -1 // Sentinel value for "Any"

                    for (i in 0 until categoriesArray.length()) {
                        val categoryObject = categoriesArray.getJSONObject(i)
                        val id = categoryObject.getInt("id")
                        val name = categoryObject.getString("name")

                        categoryList.add(name)
                        categoryMap[name] = id
                    }

                    runOnUiThread {
                        setupCategorySpinner()
                        progressBar.visibility = View.GONE
                        spinnerCategory.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun startQuiz() {
        val numberOfQuestions = spinnerNumberOfQuestions.selectedItem.toString()
        val categoryName = spinnerCategory.selectedItem.toString()
        val categoryId = categoryMap[categoryName] ?: -1
        val difficulty = spinnerDifficulty.selectedItem.toString().toLowerCase()
        val type = spinnerType.selectedItem.toString().toLowerCase()

        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("numberOfQuestions", numberOfQuestions)
            putExtra("categoryId", categoryId)
            putExtra("difficulty", difficulty)
            putExtra("type", type)
        }
        startActivity(intent)
    }
}
