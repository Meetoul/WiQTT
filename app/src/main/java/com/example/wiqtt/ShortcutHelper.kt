package com.example.wiqtt

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import com.example.wiqtt.mqtt.entities.Message

object ShortcutHelper {

    fun createMessageShortcut(context: Context, message: Message) {
        val shorcutManager = context.getSystemService(ShortcutManager::class.java)

        if (shorcutManager.isRequestPinShortcutSupported) {

            val shortcutInfo = ShortcutInfo.Builder(context, message.id.toString())
                .setShortLabel(message.name)
                .setLongLabel(message.name)
                .setRank(0)
                .build()

            shorcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }
}