package com.alpharays.autosound.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.alpharays.autosound.data.TriggerDatabase
import com.alpharays.autosound.data.trigger.Trigger
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

    fun createTrigger(trigger: Trigger) = viewModelScope.launch(Dispatchers.IO) {
        repo.createTrigger(trigger)
    }

    fun deleteTrigger(trigger: Trigger) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteTrigger(trigger)
    }

}