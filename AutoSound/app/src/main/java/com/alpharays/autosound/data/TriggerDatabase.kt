package com.alpharays.autosound.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.data.trigger.TriggerDao

@Database(entities = [Trigger::class], version = 1, exportSchema = false)
abstract class TriggerDatabase : RoomDatabase() {

    abstract fun triggerDao(): TriggerDao

    companion object {
        @Volatile
        private var INSTANCE: TriggerDatabase? = null
        fun getDatabase(context: Context): TriggerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TriggerDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}