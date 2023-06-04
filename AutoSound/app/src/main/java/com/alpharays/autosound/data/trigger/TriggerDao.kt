package com.alpharays.autosound.data.trigger

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TriggerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createTrigger(trigger: Trigger)

    @Query("SELECT * FROM sound_trigger")
    fun getAllTriggers(): LiveData<List<Trigger>>

    @Delete
    suspend fun deleteTrigger(trigger: Trigger)
}