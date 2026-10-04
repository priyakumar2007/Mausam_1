package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.MausamUiState
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AskMausamScreen(
    uiState: MausamUiState,
    onSendMessage: (String) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Text to Speech State
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var autoSpeak by remember { mutableStateOf(false) }
    var currentSpeakingMsgIndex by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(context) {
        val speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts = speech
        onDispose {
            speech.stop()
            speech.shutdown()
        }
    }

    fun speakText(text: String, lang: AppLanguage, index: Int? = null) {
        if (isTtsReady && tts != null) {
            currentSpeakingMsgIndex = index
            val locale = when (lang) {
                AppLanguage.TAMIL -> Locale("ta", "IN")
                AppLanguage.TANGLISH -> Locale("ta", "IN")
                AppLanguage.ENGLISH -> Locale("en", "IN")
            }
            tts?.language = locale
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MAUSAM_BOT_SPEECH")
        } else {
            Toast.makeText(context, "Voice engine initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        currentSpeakingMsgIndex = null
    }

    // Speech-to-Text Voice Recognition Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onSendMessage(spoken)
            }
        }
    }

    fun launchVoiceAssistant(lang: AppLanguage) {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val langTag = when (lang) {
                    AppLanguage.TAMIL -> "ta-IN"
                    AppLanguage.TANGLISH -> "ta-IN"
                    AppLanguage.ENGLISH -> "en-IN"
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, AppStrings.getVoiceListeningPrompt(lang))
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input requires Google Speech Services", Toast.LENGTH_SHORT).show()
        }
    }

    val quickQuestions = remember(uiState.language) {
        AppStrings.getQuickQuestions(uiState.language)
    }

    // Auto-scroll on new message and Auto-Speak if enabled
    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
            val lastMsg = uiState.chatMessages.last()
            if (autoSpeak && !lastMsg.isUser) {
                speakText(lastMsg.text, uiState.language, uiState.chatMessages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .testTag("ask_mausam_screen_container")
    ) {
        // Chat Header with Bot identity and Voice/Language controls
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(SkyBluePrimary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Voice Assistant",
                                tint = SkyBlueLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MAUSAM Voice AI",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RiskLowGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "3 LANGUAGES",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = RiskLowGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${uiState.currentLocation.locality} • Voice Assistant Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Auto-Speak Voice Toggle Button
                    FilterChip(
                        selected = autoSpeak,
                        onClick = {
                            autoSpeak = !autoSpeak
                            if (!autoSpeak) stopSpeaking()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Voice Output",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (autoSpeak) "Voice ON" else "Voice OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("auto_speak_toggle_chip")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Three-Language Selection Bar: English, Tamil, Tanglish
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.language == AppLanguage.TAMIL,
                        onClick = { onLanguageChange(AppLanguage.TAMIL) },
                        label = { Text("🇮🇳 தமிழ் (Tamil)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.language == AppLanguage.TANGLISH,
                        onClick = { onLanguageChange(AppLanguage.TANGLISH) },
                        label = { Text("🗣️ Tanglish") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.language == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                        label = { Text("🇬🇧 English") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(uiState.chatMessages.mapIndexed { idx, msg -> Pair(idx, msg) }) { (idx, msg) ->
                Row(
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!msg.isUser) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .background(IndigoAccent, CircleShape)
                        ) {
                            Text(text = "⛅", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (msg.isUser) SkyBluePrimary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                bottomEnd = if (msg.isUser) 4.dp else 16.dp
                            ),
                            tonalElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 285.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                                    color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                )

                                // Voice Read Aloud button for bot replies
                                if (!msg.isUser) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (currentSpeakingMsgIndex == idx) {
                                                    stopSpeaking()
                                                } else {
                                                    speakText(msg.text, uiState.language, idx)
                                                }
                                            }
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentSpeakingMsgIndex == idx) Icons.Default.Stop else Icons.Default.VolumeUp,
                                            contentDescription = "Speak",
                                            tint = SkyBlueLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (currentSpeakingMsgIndex == idx) "Stop" else "Listen",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SkyBlueLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips (Localized)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { q ->
                SuggestionChip(
                    onClick = { onSendMessage(q) },
                    label = { Text(q, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }

        // Message & Voice Input Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Voice Input Microphone Button
                IconButton(
                    onClick = { launchVoiceAssistant(uiState.language) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IndigoAccent.copy(alpha = 0.2f))
                        .testTag("voice_assistant_mic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = IndigoAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = AppStrings.getChatPlaceholder(uiState.language),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ask_mausam_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val txt = inputText
                            inputText = ""
                            onSendMessage(txt)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SkyBluePrimary)
                        .testTag("send_chat_message_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
