package com.example.wiqtt.mqtt.entities

import android.os.Parcelable
import io.requery.*

@Entity
interface Message : Parcelable, Persistable {
    @get:Key
    @get:Generated
    val id: Int

    val name: String
    val topic: String
    val payload: String

    @get:ManyToOne
    var broker: Broker
}
