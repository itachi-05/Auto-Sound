package com.alpharays.autosound.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alpharays.autosound.data.TriggerDatabase
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.data.trigger_instance.TriggerInstance
import com.alpharays.autosound.repository.TriggerInstanceRepository
import com.alpharays.autosound.repository.TriggerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TriggerViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: TriggerRepository
    val allTriggers: LiveData<List<Trigger>>

    init {
        val dao = TriggerDatabase.getDatabase(application).triggerDao()
        repo = TriggerRepository(dao)
        allTriggers = repo.triggers
    }

    fun getTriggerById(id: Long): LiveData<Trigger>{
        val liveData = MutableLiveData<Trigger>()
        viewModelScope.launch(Dispatchers.IO){
            liveData.postValue(repo.getTrigger(id))
        }
        return liveData
    }

    fun createTrigger(trigger: Trigger,  callback: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        repo.createTrigger(trigger)
        callback.invoke()
    }

    fun updateTrigger(trigger: Trigger) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateTrigger(trigger)
    }

    fun deleteTrigger(trigger: Trigger) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteTrigger(trigger)
    }

}

class TriggerInstanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: TriggerInstanceRepository
    val allTriggerInstances: LiveData<List<TriggerInstance>>

    init {
        val dao = TriggerDatabase.getDatabase(application).triggerInstanceDao()
        repo = TriggerInstanceRepository(dao)
        allTriggerInstances = repo.triggerInstances
    }

    fun createTriggerInstance(triggerInstance: TriggerInstance) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.createTriggerInstance(triggerInstance)
        }

    fun deleteTriggerInstance(triggerInstance: TriggerInstance) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteTriggerInstance(triggerInstance)
        }

}