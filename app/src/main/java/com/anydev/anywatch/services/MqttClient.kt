package com.anydev.anywatch.services

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MQTTClient(
    serverURI: String,
    clientId: String,
    private val messageCallback: (MqttMessage) -> Unit
) {
    private val client: MqttClient = MqttClient(serverURI, clientId, MemoryPersistence())

    init {
        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                println("Connection lost: ${cause?.message}")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let { messageCallback(it) }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Delivery complete")
            }
        })
    }

    fun connect() {
        val connOpts = MqttConnectOptions().apply {
            isCleanSession = true
        }
        client.connect(connOpts)
    }

    fun subscribe(topic: String, qos: Int) {
        client.subscribe(topic, qos)
    }

    fun publish(topic: String, payload: ByteArray, qos: Int, retained: Boolean) {
        val message = MqttMessage(payload).apply {
            this.qos = qos
            this.isRetained = retained
        }
        client.publish(topic, message)
    }

    fun disconnect() {
        client.disconnect()
    }
}
