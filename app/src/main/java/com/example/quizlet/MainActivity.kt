package com.example.quizlet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.quizlet.ui.theme.QuizletAppTheme
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
// Imports are se several, most of which needed to individually researched for the task at hand

// Data class for a Quizlet
data class Quizlet(
    val question: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>
    // correct answer is its own thing, but any wrong answer is in the same category. This is used later for identification.
)

// Screens Enum, used to switch between screens. THere are four screens.
enum class Screen { MAIN_MENU, ADD_QUIZLET, PRACTICE, GOODBYE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizletAppTheme {
                // State variables
                var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }
                val quizlets = remember { mutableStateListOf<Quizlet>() } // List of quizlets, changeable

                // Scaffold to manage the screen content. The html of the app.
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.MAIN_MENU -> MainMenu { currentScreen = it }
                            Screen.ADD_QUIZLET -> AddQuizlet(
                                onSave = { quizlet ->
                                    quizlets.add(quizlet)
                                    currentScreen = Screen.MAIN_MENU
                                    Toast.makeText(this@MainActivity, "Quizlet saved!", Toast.LENGTH_SHORT).show()
                                },
                                onCancel = { currentScreen = Screen.MAIN_MENU }
                            )
                            Screen.PRACTICE -> PracticeQuizlet(
                                quizlets = quizlets,
                                onReturnToMenu = { currentScreen = Screen.MAIN_MENU },
                                onDeleteQuizlet = { quizlets.remove(it) }
                            )
                            Screen.GOODBYE -> GoodbyeScreen {
                                finish() // Close the app
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenu(onOptionSelected: (Screen) -> Unit) {
    // mainly handles the transition to other screens.
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Button(onClick = { onOptionSelected(Screen.ADD_QUIZLET) }, modifier = Modifier.fillMaxWidth()) {
            Text("Add New Quizlet")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onOptionSelected(Screen.PRACTICE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Practice Quizlets")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onOptionSelected(Screen.GOODBYE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Close App")
        }
    }
}

@Composable
fun AddQuizlet(onSave: (Quizlet) -> Unit, onCancel: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var wrongAnswer1 by remember { mutableStateOf("") }
    var wrongAnswer2 by remember { mutableStateOf("") }
    var wrongAnswer3 by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = question, onValueChange = { question = it }, label = { Text("Question") }) // can expand to permit a longer response.
        TextField(value = correctAnswer, onValueChange = { correctAnswer = it }, label = { Text("Correct Answer") })
        TextField(value = wrongAnswer1, onValueChange = { wrongAnswer1 = it }, label = { Text("Wrong Answer 1") })
        TextField(value = wrongAnswer2, onValueChange = { wrongAnswer2 = it }, label = { Text("Wrong Answer 2") })
        TextField(value = wrongAnswer3, onValueChange = { wrongAnswer3 = it }, label = { Text("Wrong Answer 3") })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onSave(Quizlet(question, correctAnswer, listOf(wrongAnswer1, wrongAnswer2, wrongAnswer3)))
        }) {
            Text("Save Quizlet")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@Composable
fun PracticeQuizlet(
    quizlets: List<Quizlet>,
    onReturnToMenu: () -> Unit,
    onDeleteQuizlet: (Quizlet) -> Unit
) {
    if (quizlets.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No quizlets available. Add some first!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onReturnToMenu) { Text("Back to Menu") }
        }
        return
    }

    var showMessage by remember { mutableStateOf(false) }
    var showWrongAnswerScreen by remember { mutableStateOf(false) }
    val randomQuizlet = remember { quizlets.random() } // Retain the same quizlet

    if (showWrongAnswerScreen) {
        // Dark screen with "Not quite..." message
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Not quite...", color = Color.White, style = MaterialTheme.typography.headlineLarge)

            // Start the timer to reset the screen
            LaunchedEffect(Unit) {
                delay(5000) // 5 seconds delay
                showWrongAnswerScreen = false // Return to the quizlet question
            }
        }
    } else {
        // Main quizlet practice UI
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = randomQuizlet.question, modifier = Modifier.padding(bottom = 16.dp))

            val options = (randomQuizlet.wrongAnswers + randomQuizlet.correctAnswer).shuffled()
            options.forEach { answer ->
                Button(
                    onClick = {
                        if (answer == randomQuizlet.correctAnswer) {
                            showMessage = true
                        } else {
                            showWrongAnswerScreen = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(answer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (showMessage) {
                Text("Good job!", color = Color.Blue, modifier = Modifier.padding(bottom = 16.dp))

                // Timer for "Good job!" message
                LaunchedEffect(Unit) {
                    delay(5000) // 5 seconds delay
                    showMessage = false
                }
            }

            Button(
                onClick = { onDeleteQuizlet(randomQuizlet) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Delete Quizlet")
            }
            Button(onClick = onReturnToMenu, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Back to Menu")
            }
        }
    }
}



@Composable
fun GoodbyeScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(5000) // 5 seconds
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Goodbye!", fontSize = 24.sp, color = Color.Red)
    }
}
