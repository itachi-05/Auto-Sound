package com.alpharays.autosound.data.trigger_instance


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "sound_trigger_instance")
data class TriggerInstance(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "trigger_id") var triggerId: Long = 0
)