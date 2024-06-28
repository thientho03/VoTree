package com.example.votree.tips

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val message = "Language is not supported"
                speak(message)
            }
        } else {
            val message = "Initialization failed"
            speak(message)
        }
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speak(textTitle: String, textAuthor: String, textContent: String) {
        val introduction = "Now, let's hear about:"
        val transition = "Written by:"
        val conclusion = "That's all for now."

        textToSpeech?.speak(introduction, TextToSpeech.QUEUE_FLUSH, null, null)
        textToSpeech?.speak(textTitle, TextToSpeech.QUEUE_ADD, null, null)

        textToSpeech?.speak(transition, TextToSpeech.QUEUE_ADD, null, null)
        textToSpeech?.speak(textAuthor, TextToSpeech.QUEUE_ADD, null, null)

        textToSpeech?.speak(textContent, TextToSpeech.QUEUE_ADD, null, null)
        textToSpeech?.speak(conclusion, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}