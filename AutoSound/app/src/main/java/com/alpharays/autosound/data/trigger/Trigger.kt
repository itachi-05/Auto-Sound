package com.alpharays.autosound.data.trigger


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alpharays.autosound.util.Constants
import java.util.Date
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.io.Serializable

@Entity(tableName = "sound_trigger")
@TypeConverters(DateConverter::class)
data class Trigger(
    @ColumnInfo(name = "is_repeat") var isRepeat: Boolean = false,
    @ColumnInfo(name = "days_of_week") var daysOfWeek: String = "",
    @ColumnInfo(name = "trigger_time") var triggerTime: String = "",
    @ColumnInfo(name = "trigger_date_time") var triggerDateTime: Date? = null,
    @ColumnInfo(name = "ringer_mode") var ringerMode: String = Constants.RingerMode.Normal.name,
    @ColumnInfo(name = "ringer_volume") var ringerVolume: Int = 0,
    @ColumnInfo(name = "media_volume") var mediaVolume: Int = 0,
    @ColumnInfo(name = "alarm_volume") var alarmVolume: Int = 0
): Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}


class DateConverter {
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}