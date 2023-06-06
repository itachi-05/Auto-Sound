package com.alpharays.autosound.data.trigger

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import java.util.Date

@Dao
@TypeConverters(DateConverter::class)
interface TriggerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createTrigger(trigger: Trigger)

    @Query("UPDATE sound_trigger SET is_repeat=:isRepeat, days_of_week=:daysOfWeek, trigger_time=:triggerTime, trigger_date_time=:triggerDateTime, ringer_mode=:ringerMode, ringer_volume=:ringerVolume, media_volume=:mediaVolume, alarm_volume=:alarmVolume WHERE id=:id")
    suspend fun updateTrigger(
        id: Long,
        isRepeat: Boolean,
        daysOfWeek: String,
        triggerTime: String,
        triggerDateTime: Date?,
        ringerMode: String,
        ringerVolume: Int,
        mediaVolume: Int,
        alarmVolume: Int
    )

    @Query("SELECT * FROM sound_trigger")
    fun getAllTriggers(): LiveData<List<Trigger>>

    @Delete
    suspend fun deleteTrigger(trigger: Trigger)
}