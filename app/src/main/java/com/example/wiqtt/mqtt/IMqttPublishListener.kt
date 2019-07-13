package com.example.wiqtt.mqtt

import com.example.wiqtt.mqtt.entities.Message

interface IMqttPublishListener {
    fun onPublishSuccess(message: Message)
    fun onPublishFailed(message: Message, exception: Throwable?)
}