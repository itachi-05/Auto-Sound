package com.alpharays.autosound.data.trigger_instance

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TriggerInstanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createTrigger(trigger: TriggerInstance)

    @Query("SELECT * FROM sound_trigger")
    fun getAllTriggers(): LiveData<List<TriggerInstance>>

    @Delete
    suspend fun deleteTrigger(trigger: TriggerInstance)
}
