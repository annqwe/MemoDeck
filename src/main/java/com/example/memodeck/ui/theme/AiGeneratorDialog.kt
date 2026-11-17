package com.example.memodeck.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGeneratorDialog(
    onDismiss: () -> Unit,
    onGenerate: (String, String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // ВНИМАНИЕ: Вставь сюда свой бесплатный API-ключ от Google AI Studio
    // (https://aistudio.google.com/app/apikey)
    val apiKey = "ВСТАВЬ_СВОЙ_КЛЮЧ_СЮДА"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✨ Магия ИИ", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("Введи тему, и ИИ создаст карточку для тебя:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Например: Столицы Европы") },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        isLoading = true
                        errorMsg = null
                        scope.launch {
                            try {
                                val model = GenerativeModel("gemini-pro", apiKey = apiKey)
                                val prompt = "Создай ровно один вопрос и короткий ответ на русском языке по теме: '$topic'. Формат ответа строго: Вопрос | Ответ"

                                val response = withContext(Dispatchers.IO) {
                                    model.generateContent(prompt)
                                }

                                val text = response.text
                                if (text != null) {
                                    val parts = text.split("|")
                                    if (parts.size >= 2) {
                                        onGenerate(parts[0].trim(), parts[1].trim())
                                        onDismiss()
                                    } else {
                                        errorMsg = "Не удалось распознать формат ответа."
                                    }
                                }
                            } catch (e: Exception) {
                                errorMsg = "Ошибка: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading && topic.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Создать")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Отмена") } },
        shape = RoundedCornerShape(24.dp)
    )
}