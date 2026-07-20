package com.example.gamestorehb.ui.risk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskProfileScreen(
    onComplete: () -> Unit,
    viewModel: RiskProfileViewModel = hiltViewModel()
) {
    val answers by viewModel.answers.collectAsStateWithLifecycle()
    val isComplete by viewModel.isComplete.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(isComplete) {
        if (isComplete) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Profiling") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Discover your investor profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            viewModel.questions.forEachIndexed { qIndex, question ->
                Text(
                    text = "${qIndex + 1}. $question",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val currentAnswer = answers.getOrNull(qIndex)
                
                viewModel.options[qIndex].forEachIndexed { aIndex, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.answerQuestion(qIndex, aIndex) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentAnswer == aIndex,
                            onClick = { viewModel.answerQuestion(qIndex, aIndex) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitAnswers() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp), // Extra padding for bottom nav
                enabled = answers.size == viewModel.questions.size
            ) {
                Text("Complete Profile")
            }
        }
    }
}
