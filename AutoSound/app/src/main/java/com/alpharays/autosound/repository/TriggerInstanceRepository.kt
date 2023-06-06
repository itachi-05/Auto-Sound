package com.alpharays.autosound.repository

import androidx.lifecycle.LiveData
import com.alpharays.autosound.data.trigger_instance.TriggerInstance
import com.alpharays.autosound.data.trigger_instance.TriggerInstanceDao

class TriggerInstanceRepository(private val triggerInstanceDao: TriggerInstanceDao) {
    val triggerInstances: LiveData<List<TriggerInstance>> = triggerInstanceDao.getAllTriggers()

    suspend fun createTriggerInstance(triggerInstance: TriggerInstance) {
        triggerInstanceDao.createTrigger(triggerInstance)
    }

    suspend fun deleteTriggerInstance(triggerInstance: TriggerInstance) {
        triggerInstanceDao.deleteTrigger(triggerInstance)
    }
}