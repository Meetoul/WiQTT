package com.example.wiqtt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.wiqtt.mqtt.entities.Message
import com.example.wiqtt.mqtt.entities.MessageEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MessageCreationActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_creation)

        val saveButton: FloatingActionButton = findViewById(R.id.message_save_button)

        saveButton.setOnClickListener(this)
    }

    private fun createMessage(): Message {
        val nameInput: EditText = findViewById(R.id.message_name_input)
        val topicInput: EditText = findViewById(R.id.message_topic_input)
        val payloadInput: EditText = findViewById(R.id.message_payload_input)

        val message = MessageEntity()

        message.setName(nameInput.text.toString())
        message.setTopic(topicInput.text.toString())
        message.setPayload(payloadInput.text.toString())

        return message
    }

    override fun onClick(p0: View?) {
        val message = createMessage()

        val resultCode = 0
        val resultIntent = Intent()
        resultIntent.putExtra(resources.getString(R.string.message_intent_extra), message)
        setResult(resultCode, resultIntent)
        finish()
    }
}
