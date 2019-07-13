package com.example.wiqtt.mqtt

import com.example.wiqtt.mqtt.entities.Broker

interface IMqttConnectListener {
    fun onConnectSuccess(broker: Broker)
    fun onConnectFailed(broker: Broker, exception: Throwable?)
    fun onDisconnectSuccess(broker: Broker)
    fun onDisconnectFailed(broker: Broker, exception: Throwable?)
}