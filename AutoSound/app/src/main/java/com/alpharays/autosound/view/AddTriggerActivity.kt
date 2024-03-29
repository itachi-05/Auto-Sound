package com.alpharays.autosound.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.alpharays.autosound.R
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.databinding.ActivityAddTriggerBinding
import com.alpharays.autosound.util.Constants
import com.alpharays.autosound.util.Utilities
import com.alpharays.autosound.viewmodel.TriggerViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Date
import kotlin.collections.ArrayList

class AddTriggerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTriggerBinding
    private var isExiting = false
    private lateinit var checkBoxes: Array<CheckBox>
    private var numberOfCheckBoxesChecked = 0
    private var timeSelected = ""
    private var dateSelected: Date? = null
    private var ringerVolumeSelected = 0
    private var mediaVolumeSelected = 0
    private var alarmVolumeSelected = 0
    private var isRepeatTriggerCheck = false
    private var daysSelected: ArrayList<Int> = ArrayList()
    private var ringerModeSelected = Utilities.ringerModes[0]
    private var datePicker: MaterialDatePicker<Long>? = null
    private lateinit var timePicker: MaterialTimePicker
    private var repeatSwitchObserver: MutableLiveData<Boolean> = MutableLiveData(false)
    private var ringerModeObserver: MutableLiveData<String> =
        MutableLiveData(Utilities.ringerModes[0])
    private val triggerViewModel: TriggerViewModel by lazy { ViewModelProvider(this)[TriggerViewModel::class.java] }

    /*
    Required items to create a Trigger:
    1) if isRepeat: not need to select data : else need to select
    2) ringer mode is by default set to Normal(General) mode
    3) ringer / media / alarm Volume - can be 0 or anything
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTriggerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        for (i in 0..6) daysSelected.add(0)

        checkBoxes = arrayOf(
            binding.sunday,
            binding.monday,
            binding.tuesday,
            binding.wedday,
            binding.thurday,
            binding.friday,
            binding.satday
        )

        // 1st layout : repeat switch
        binding.repeatSwitch.setOnCheckedChangeListener { _, isChecked ->
            repeatSwitchObserver.postValue(isChecked)
        }

        repeatSwitchObserver.observe(this) {
            it?.let { repeat ->
                isRepeatTriggerCheck = repeat
                if (repeat) {
                    // 2nd layout : check boxes
                    for (checkBox in checkBoxes) checkBox.isEnabled = true
                    for (checkBox in checkBoxes) {
                        checkBox.setOnClickListener {
                            if (checkBox.isChecked) {
                                val index = getIndex(checkBox)
                                if (index != -1) {
                                    numberOfCheckBoxesChecked++
                                    daysSelected[index] = 1
                                }
                            } else {
                                val index = getIndex(checkBox)
                                if (index != -1) {
                                    numberOfCheckBoxesChecked--
                                    daysSelected[index] = 0
                                }
                            }
                        }
                    }

                    // 3rd layout : date & time picker with Date enabled
                    dateAndTimePicker(true)
                } else {
                    // Remove click listeners when isRepeat is true : Related to 2nd layout
                    for (checkBox in checkBoxes) {
                        checkBox.setOnClickListener(null)
                        checkBox.isEnabled = false
                    }
                    // 3rd layout : date & time picker with Date disabled
                    dateAndTimePicker(false)
                }
            }
        }


        // 4th layout : ringer modes
        val adapter = ArrayAdapter(this, R.layout.ringer_modes_item, Utilities.ringerModes)
        binding.autoCompleteRingerMode.setAdapter(adapter)
        binding.autoCompleteRingerMode.setText(Constants.RingerMode.Normal.name, false)
        binding.autoCompleteRingerMode.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            // Convert the selected item string to the corresponding RingerMode enum value
            val selectedRingerMode = Constants.RingerMode.valueOf(selectedItem)
            ringerModeSelected = selectedRingerMode.toString()
            ringerModeObserver.postValue(selectedRingerMode.toString())
        }


        ringerModeObserver.observe(this) {
            it?.let { ringerMode ->
                if (ringerMode == Utilities.ringerModes[0]) {
                    binding.ringerVolumeLL.visibility = View.VISIBLE
                } else {
                    binding.ringerVolumeLL.visibility = View.GONE
                }
                binding.ringerVolumeSeekBar.progress = 0
                ringerVolumeSelected = 0
            }
        }


        // 5th layout : ringer volume
        binding.ringerVolumeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Handle progress change
                val volumeValue = progress // The current value of the SeekBar
                // Use the volumeValue as needed
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when user starts tracking touch on the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when user stops tracking touch on the SeekBar
                val value = seekBar?.progress ?: 0
                ringerVolumeSelected = value
            }
        })


        // 6th layout : media volume
        binding.mediaVolumeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Handle progress change
                val volumeValue = progress // The current value of the SeekBar
                // Use the volumeValue as needed
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when user starts tracking touch on the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when user stops tracking touch on the SeekBar
                val value = seekBar?.progress ?: 0
                mediaVolumeSelected = value
            }
        })


        // 7th layout : alarm volume
        binding.alarmVolumeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Handle progress change
                val volumeValue = progress // The current value of the SeekBar
                // Use the volumeValue as needed
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when user starts tracking touch on the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when user stops tracking touch on the SeekBar
                val value = seekBar?.progress ?: 0
                alarmVolumeSelected = value
            }
        })


        // 8th layout : create trigger button
        binding.createTrigger.setOnClickListener {
            if (isRepeatTriggerCheck) {
                if (numberOfCheckBoxesChecked == 0) {
                    showSnackBar("Select days to create Trigger")
                }
                else if(timeSelected=="" || timeSelected.isEmpty()){
                    showSnackBar("Select time to create Trigger")
                }
                else if (timeSelected != "" && timeSelected.isNotEmpty()) {
                    var daysOfWeek = ""
                    for (days in daysSelected.indices) daysOfWeek += daysSelected[days].toString()
                    val trigger = Trigger(
                        true,
                        daysOfWeek,
                        timeSelected,
                        null,
                        ringerModeSelected,
                        ringerVolumeSelected,
                        mediaVolumeSelected,
                        alarmVolumeSelected
                    )
                    triggerViewModel.createTrigger(trigger)
                    showSnackBar("Trigger Created")
                    exitScreen()
                } else {
                    showSnackBar("Select time to create Trigger")
                }
            } else {
                if (dateSelected == null) {
                    showSnackBar("Select date to create Trigger")
                } else if (timeSelected == "" || timeSelected.isEmpty()) {
                    showSnackBar("Select time to create Trigger")
                } else if (timeSelected != "" && timeSelected.isNotEmpty() && dateSelected != null) {
                    val trigger = Trigger(
                        false,
                        "",
                        timeSelected,
                        dateSelected,
                        ringerModeSelected,
                        ringerVolumeSelected,
                        mediaVolumeSelected,
                        alarmVolumeSelected
                    )
                    triggerViewModel.createTrigger(trigger)
                    showSnackBar("Trigger Created")
                    exitScreen()
                } else {
                    try {
                        showSnackBar("triggerError#1: $isRepeatTriggerCheck : $numberOfCheckBoxesChecked : $timeSelected : $dateSelected : $ringerModeSelected : $ringerVolumeSelected : $mediaVolumeSelected : $alarmVolumeSelected")
                    } catch (e: Exception) {
                        Log.i("exceptionTrigger$1", e.message.toString())
                    }
                    showSnackBar("Something went wrong")
                }
            }
        }


        // 9th layout : cancel trigger button
        binding.cancelTrigger.setOnClickListener {
            showSnackBar("Trigger Cancelled")
            exitScreen()
        }

    }

    private fun dateAndTimePicker(repeat: Boolean) {
        // Date Picker
        val dateBuilder = MaterialDatePicker.Builder.datePicker()
        if (!repeat) {
            datePicker = dateBuilder.build()
            binding.chooseDateBtn.isEnabled = true
            binding.chooseDateBtn.setOnClickListener {
                datePicker?.show(supportFragmentManager, "datePicker")
            }

            // Set Date Picker Listener
            datePicker?.addOnPositiveButtonClickListener { selectedDate ->
                val selectedMillis = datePicker?.selection ?: 0L
                dateSelected = Date(selectedMillis)
                binding.chooseDateBtn.text = datePicker?.headerText.toString()
            }
        } else {
            binding.chooseDateBtn.isEnabled = false
            binding.chooseDateBtn.text = "Select Date"
            dateSelected = null
            datePicker = null
        }

        // Time Picker
        val timeBuilder = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText("Select Time")
            .build()

        timePicker = timeBuilder
        binding.chooseTimeBtn.setOnClickListener {
            timePicker.show(supportFragmentManager, "timePicker")
        }

        // Set Time Picker Listener
        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val hour = when {
                selectedHour == 0 -> 12 // Midnight
                selectedHour > 12 -> selectedHour - 12 // Afternoon or Evening
                else -> selectedHour // Morning
            }

            val timeSuffix = if (selectedHour >= 12) "PM" else "AM"
            val formattedTime = String.format("%02d:%02d %s", hour, selectedMinute, timeSuffix)
            timeSelected = formattedTime
            binding.chooseTimeBtn.text = formattedTime
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    private fun exitScreen() {
        if (!isExiting) {
            isExiting = true
            startActivity(Intent(this@AddTriggerActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finishAffinity()
        }
    }


    private fun getIndex(checkBox: CheckBox): Int {
        for (i in checkBoxes.indices) {
            if (checkBoxes[i] == checkBox) {
                return i
            }
        }
        return -1 // CheckBox not found
    }

}