package com.example.wiqtt.mqtt.entities

import android.os.Parcelable
import io.requery.*

@Entity
interface Broker : Parcelable, Persistable {

    @get:Key
    @get:Generated
    val id: Int

    val name: String
    val host: String
    val port: Int
    val protocol: Protocol

    @get:OneToMany(mappedBy = "broker")
    val messages: MutableList<Message>
}