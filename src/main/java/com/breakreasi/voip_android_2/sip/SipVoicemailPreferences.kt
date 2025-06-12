package com.breakreasi.voip_android_2.sip

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat


class SipVoicemailPreferences {
    companion object {
        private val PREFS_NAME: String = "sipvoip_voicemail_prefs"
        private val LIST_KEY: String = "sipvoip_voicemail_list"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun getList(context: Context?): MutableList<SipVoicemailModel>? {
            val prefs = getSharedPreferences(context!!)
            val json = prefs.getString(LIST_KEY, null)
            if (json == null || json.isEmpty()) {
                return ArrayList<SipVoicemailModel>()
            }
            val gson = Gson()
            return gson.fromJson<MutableList<SipVoicemailModel>?>(
                json,
                object : TypeToken<MutableList<SipVoicemailModel>?>() {}.getType()
            )
        }

        @SuppressLint("SimpleDateFormat")
        fun save(context: Context, name: String, from: String, url: String) {
            val list = getList(context)
            val voicemailModel = SipVoicemailModel()
            voicemailModel.name = name
            voicemailModel.from = from
            voicemailModel.url = url
            voicemailModel.date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis())
            list?.add(voicemailModel)
            val gson = Gson()
            val json = gson.toJson(list)
            getSharedPreferences(context).edit {
                putString(LIST_KEY, json)
            }
        }

        fun updateDownloaded(context: Context, name: String, downloaded: Boolean, dir: String): Boolean {
            val list = getList(context)
            if (list == null) return false
            val index = list.indexOfFirst { it.name == name }
            if (index == -1) return false
            list[index].isDownloaded = downloaded
            list[index].dir = dir
            val json = Gson().toJson(list)
            getSharedPreferences(context).edit {
                putString(LIST_KEY, json)
            }
            return true
        }
    }
}