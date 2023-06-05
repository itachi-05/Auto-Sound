package com.alpharays.autosound.adapter

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.databinding.AppTriggersViewBinding
import com.alpharays.autosound.util.Constants
import com.alpharays.autosound.util.Utilities
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class TriggerCardsAdapter(private var triggers: MutableList<Trigger>) :
    RecyclerView.Adapter<TriggerCardsAdapter.TriggerCardViewHolder>() {

    private var expandedPosition = -1

    private var actionEdit: ((Trigger)->Unit)? = null
    private var actionDelete: ((Trigger)->Unit)? = null
    inner class TriggerCardViewHolder(private val binding: AppTriggersViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val triggerDateTextView = binding.triggerDateTxt
        val triggerTimeTextView = binding.triggerTimeTxt
        val ringerModeTextView = binding.ringerModeTxt
        val ringerVolumeTextView = binding.ringerVolumeTv
        val triggerCardContentLayout = binding.triggerCardContentLayout
        val ringerVolumePBar = binding.ringerVolumePbar
        val mediaVolumePBar = binding.mediaVolumePbar
        val alarmVolumePBar = binding.alarmVolumePbar
        val triggerTimeExpired = binding.triggerTimeExpired

        val actionEdit = binding.editTrigger
        val actionDelete = binding.deleteTrigger
        fun bind(trigger: Trigger) {
            binding.apply {
                triggerDateTxt.text = trigger.triggerDateTime.toString()
                triggerTimeTxt.text = trigger.triggerTime
                ringerModeTxt.text = trigger.ringerMode
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TriggerCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AppTriggersViewBinding.inflate(inflater, parent, false)
        return TriggerCardViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TriggerCardViewHolder, position: Int) {
        holder.bind(triggers[position])
        val trigger = triggers[position]
        val adapterPosition = holder.adapterPosition

        val date = StringBuilder()
        val timeTobeShown = StringBuilder()
        if (trigger.isRepeat) {
            var count = 0
            for (i in trigger.daysOfWeek.indices) {
                if (trigger.daysOfWeek[i] == '1') {
                    if (count++ > 0) date.append("/")
                    date.append(Utilities.getDayString(i, 3))
                }
            }
        } else {
            trigger.triggerDateTime?.let { dateTime ->
                val pattern = "EEE MMM dd HH:mm:ss z yyyy"
                val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                val parsedDateTime = LocalDateTime.parse(dateTime.toString(), formatter)

                val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM", Locale.getDefault())
                val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())
                val yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

                date.append(parsedDateTime.format(dateFormatter))
                    .append(" ")
                    .append(Utilities.attachSuperscriptToNumber(parsedDateTime.dayOfMonth))
                    .append(", ")
                    .append(parsedDateTime.format(yearFormatter))

            }
        }

        // reorganizing time to be set
        val timeParts = trigger.triggerTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].substringBefore(" ").toInt()
        val period = timeParts[1].substringAfter(" ")

        val localTime = if (period.equals("AM", ignoreCase = true) && hour == 12) {
            // Handle midnight (12 AM)
            LocalTime.MIDNIGHT
        } else if (period.equals("PM", ignoreCase = true) && hour < 12) {
            // Convert PM hours to 24-hour format
            LocalTime.of(hour + 12, minute)
        } else {
            // Use the provided hour and minute
            LocalTime.of(hour, minute)
        }
        val dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        timeTobeShown.append(localTime.format(dateTimeFormatter).uppercase(Locale.ROOT))

        val dateToBeShown = if (trigger.isRepeat) "$date (Repeat)" else date.toString()
//        val currentCalendar = Calendar.getInstance()
//        val dt = trigger.triggerDateTime
//        val dt2 = currentCalendar.time
//
//        dt?.let {
//            if (it < dt2) {
//                holder.triggerTimeExpired.text = "Expired"
//            } else if (it >= dt2) {
//                holder.triggerTimeExpired.text = "Upcoming"
//            }
//        }
        holder.triggerDateTextView.text = dateToBeShown
        holder.triggerTimeTextView.text = timeTobeShown.toString()
        holder.ringerModeTextView.text = trigger.ringerMode

        if (trigger.ringerMode == Constants.RingerMode.Normal.name) {
            holder.ringerVolumePBar.progress = trigger.ringerVolume
        } else {
            holder.ringerVolumeTextView.visibility = View.GONE
            holder.ringerVolumePBar.visibility = View.GONE
        }

        holder.mediaVolumePBar.progress = trigger.mediaVolume
        holder.alarmVolumePBar.progress = trigger.alarmVolume

        val isExpanded = adapterPosition == expandedPosition
        holder.triggerCardContentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.itemView.isActivated = isExpanded
        holder.itemView.tag = holder.adapterPosition

        holder.itemView.setOnClickListener { view ->
            Log.d(TAG, "onClick: Clicked on position: $adapterPosition")
            expandedPosition = if (isExpanded) -1 else view.tag as Int

            val recyclerView = view.parent as? RecyclerView
            recyclerView?.let {
                val transition = ChangeBounds().apply {
                    duration = 500 // Adjust the duration as needed
                }
                TransitionManager.beginDelayedTransition(recyclerView, transition)
                notifyDataSetChanged()
            }
        }
        holder.actionEdit.setOnClickListener {actionEdit?.invoke(trigger) }
        holder.actionDelete.setOnClickListener {actionDelete?.invoke(trigger) }
    }
    fun setOnActionEditListener(callback: (Trigger)->Unit){
        this.actionEdit = callback
    }

    fun setOnActionDeleteListener(callback: (Trigger) -> Unit){
        this.actionDelete = callback
    }
    override fun getItemCount(): Int {
        return triggers.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
