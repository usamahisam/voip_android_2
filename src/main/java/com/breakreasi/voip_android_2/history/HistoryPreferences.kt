package com.breakreasi.voip_android_2.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat


class HistoryPreferences {
    companion object {
        private val PREFS_NAME: String = "MyPrefs"
        private val LIST_KEY: String = "MyList"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun getList(context: Context?): MutableList<HistoryModel>? {
            val prefs = getSharedPreferences(context!!)
            val json = prefs.getString(LIST_KEY, null)
            if (json == null || json.isEmpty()) {
                return ArrayList<HistoryModel>()
            }
            val gson = Gson()
            return gson.fromJson<MutableList<HistoryModel>?>(
                json,
                object : TypeToken<MutableList<HistoryModel>?>() {}.getType()
            )
        }

        @SuppressLint("SimpleDateFormat")
        fun save(context: Context, name: String, description: String) {
            val list = getList(context)
            val history = HistoryModel()
            history.name = name
            history.description = description
            history.date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis())
            list?.add(history)
            val gson = Gson()
            val json = gson.toJson(list)
            getSharedPreferences(context).edit {
                putString(LIST_KEY, json)
            }
        }
    }
}