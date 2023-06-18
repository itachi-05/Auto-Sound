package com.alpharays.autosound.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.data.trigger.TriggerDao


class TriggerRepository(private val triggerDao: TriggerDao) {
    val triggers: LiveData<List<Trigger>> = triggerDao.getAllTriggers()

    suspend fun createTrigger(trigger: Trigger) {
        triggerDao.createTrigger(trigger)
    }

    suspend fun getTrigger(id: Long) : Trigger{
        return triggerDao.getTrigger(id)
    }

    suspend fun updateTrigger(trigger: Trigger) {
        triggerDao.updateTrigger(
            trigger.id,
            trigger.isRepeat,
            trigger.daysOfWeek,
            trigger.triggerTime,
            trigger.triggerDateTime,
            trigger.ringerMode,
            trigger.ringerVolume,
            trigger.mediaVolume,
            trigger.alarmVolume
        )
    }

    suspend fun deleteTrigger(trigger: Trigger) {
        triggerDao.deleteTrigger(trigger)
    }

//    private class deleteWordAsyncTask internal constructor(dao: TriggerDao) :
//        AsyncTask<Trigger?, Void?, Void?>() {
//        private val mAsyncTaskDao: TriggerDao
//
//        init {
//            mAsyncTaskDao = dao
//        }
//
//        override fun doInBackground(vararg params: Trigger): Void? {
//            mAsyncTaskDao.deleteTrigger(params[0])
//            return null
//        }
//    }
}