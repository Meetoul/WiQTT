package com.example.wiqtt.mqtt

import android.content.Context
import com.example.wiqtt.brokerAsUri
import com.example.wiqtt.mqtt.entities.Broker
import com.example.wiqtt.mqtt.entities.Message
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttClient {

    private var mContext: Context
    private var mClientId: String
    private var mConnectListener: IMqttConnectListener
    private var mPublishListener: IMqttPublishListener
    private lateinit var mBroker: Broker

    private var mMqttAndroidClient: MqttAndroidClient? = null

    constructor(
        context: Context,
        clientId: String,
        connectListener: IMqttConnectListener,
        publishListener: IMqttPublishListener
    ) {
        mContext = context
        mClientId = clientId
        mConnectListener = connectListener
        mPublishListener = publishListener
    }

    fun connect(broker: Broker) {
        disconnect()
        mBroker = broker
        mMqttAndroidClient = MqttAndroidClient(mContext, brokerAsUri(mBroker), mClientId)

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        mMqttAndroidClient?.let {
            it.connect(mqttConnectOptions, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    mConnectListener.onConnectSuccess(mBroker)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    mConnectListener.onConnectFailed(mBroker, exception)
                }
            })
        }
    }

    fun disconnect() {
        mMqttAndroidClient?.let {
            it.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    mConnectListener.onDisconnectSuccess(mBroker)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    mConnectListener.onDisconnectFailed(mBroker, exception)
                }
            })
        }
        mMqttAndroidClient = null
    }

    fun publishMessage(message: Message) {
        val mqttMessage = MqttMessage(message.payload.toByteArray())
        mMqttAndroidClient?.let {
            it.publish(message.topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    mPublishListener.onPublishSuccess(message)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    mPublishListener.onPublishFailed(message, exception)
                }
            })
        }
    }


}