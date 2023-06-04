package com.alpharays.autosound.repository

import androidx.lifecycle.LiveData
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.data.trigger.TriggerDao


class TriggerRepository(private val triggerDao: TriggerDao) {
    val triggers: LiveData<List<Trigger>> = triggerDao.getAllTriggers()

    suspend fun createTrigger(trigger: Trigger) {
        triggerDao.createTrigger(trigger)
    }

    suspend fun deleteTrigger(trigger: Trigger) {
        triggerDao.deleteTrigger(trigger)
    }
}