package com.breakreasi.voip_android_2.voip

import com.breakreasi.voip_android_2.history.HistoryEntity
import com.breakreasi.voip_android_2.history.HistoryRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class VoipDatabase {
    private val configRealm = RealmConfiguration.Builder(setOf(HistoryEntity::class))
        .schemaVersion(1)
        .build()
    private val realm = Realm.open(configRealm)
    var historyRepository = HistoryRepository(realm)
}