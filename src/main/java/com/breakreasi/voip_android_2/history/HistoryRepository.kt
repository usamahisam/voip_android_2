package com.breakreasi.voip_android_2.history

import io.realm.kotlin.Realm

class HistoryRepository(private val realm: Realm) {
    suspend fun save(history: HistoryEntity) {
        realm.write {
            copyToRealm(history)
        }
    }

    fun getAll(): List<HistoryEntity> {
        return realm.query(HistoryEntity::class).find().toList()
    }

    suspend fun deleteAll() {
        realm.write {
            val histories = query(HistoryEntity::class).find()
            delete(histories)
        }
    }

}