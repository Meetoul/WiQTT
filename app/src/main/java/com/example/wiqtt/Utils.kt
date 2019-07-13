package com.example.wiqtt

import com.example.wiqtt.mqtt.entities.Broker

fun brokerAsUri(broker: Broker): String {
    return "${broker.protocol.str}://${broker.host}:${broker.port}"
}