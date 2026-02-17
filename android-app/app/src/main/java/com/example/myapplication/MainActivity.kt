package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.net.Socket
import java.util.*

class MainActivity : AppCompatActivity() {

    // --- Configuration ---
    // Change this to your Arduino's IP shown in the Serial Monitor
    private val ARDUINO_IP = "192.168.1.148" 
    private val ARDUINO_PORT = 8080
    
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var btnSpeak: Button
    private lateinit var btnSend: Button
    private lateinit var tvResult: TextView
    private val speechBuffer = StringBuilder()
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI Elements
        btnSpeak = findViewById(R.id.botao_falar)
        btnSend = findViewById(R.id.botao_enviar)
        tvResult = findViewById(R.id.texto_reconhecido)

        btnSpeak.setOnClickListener {
            if (isListening) stopListening() 
            else checkPermissionAndStart()
        }

        btnSend.setOnClickListener {
            val message = tvResult.text.toString().trim()
            if (message.isNotEmpty()) {
                sendToArduino(message)
            } else {
                Toast.makeText(this, "No text to send", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        isListening = true
        btnSpeak.text = "Recording..."
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-PT") // Adjusted for Portugal
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                btnSpeak.text = "Start Recognition"
            }
            override fun onError(error: Int) {
                Log.e("Speech", "Error code: $error")
                isListening = false
                btnSpeak.text = "Start Recognition"
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    tvResult.text = matches[0]
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        btnSpeak.text = "Start Recognition"
    }

    /**
     * Sends the recognized text to the Arduino Server via TCP Socket
     * and waits for the AI response.
     */
    private fun sendToArduino(message: String) {
        Thread {
            try {
                val socket = Socket(ARDUINO_IP, ARDUINO_PORT)
                socket.soTimeout = 10000 // 10s timeout for AI processing

                // Send Data
                val output: OutputStream = socket.getOutputStream()
                output.write((message + "\n").toByteArray())
                output.flush()

                // Read AI Response from Arduino
                val input = socket.getInputStream().bufferedReader()
                val response = input.readLine()

                runOnUiThread {
                    tvResult.text = "Gemini: $response"
                }

                socket.close()
            } catch (e: Exception) {
                Log.e("Network", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this, "Failed to connect to Arduino", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}