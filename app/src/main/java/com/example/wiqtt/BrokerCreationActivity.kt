package com.example.wiqtt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import com.example.wiqtt.mqtt.entities.Broker
import com.example.wiqtt.mqtt.entities.BrokerEntity
import com.example.wiqtt.mqtt.entities.Protocol
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BrokerCreationActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broker_creation)

        val protocolSpinner: Spinner = findViewById(R.id.broker_protocol_spinner)
        ArrayAdapter(
            this, R.layout.support_simple_spinner_dropdown_item,
            Protocol.values()
        ).also {
            protocolSpinner.adapter = it
        }

        val saveButton: FloatingActionButton = findViewById(R.id.broker_save_button)

        saveButton.setOnClickListener(this)
    }

    private fun createBroker(): Broker {
        val nameInput: EditText = findViewById(R.id.broker_name_input)
        val hostInput: EditText = findViewById(R.id.broker_host_input)
        val portInput: EditText = findViewById(R.id.broker_port_input)
        val protocolSpinner: Spinner = findViewById(R.id.broker_protocol_spinner)

        val brokerEntity = BrokerEntity()

        brokerEntity.setName(nameInput.text.toString())
        brokerEntity.setHost(hostInput.text.toString())
        brokerEntity.setPort(portInput.text.toString().toInt())
        brokerEntity.setProtocol(Protocol.valueOf(protocolSpinner.selectedItem.toString()))

        return brokerEntity
    }

    override fun onClick(p0: View?) {
        val broker = createBroker()

        val resultCode = 0
        val resultIntent = Intent()
        resultIntent.putExtra(resources.getString(R.string.broker_intent_extra), broker)
        setResult(resultCode, resultIntent)
        finish()
    }
}
