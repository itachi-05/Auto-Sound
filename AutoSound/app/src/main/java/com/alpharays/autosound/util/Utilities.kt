package com.alpharays.autosound.util

object Utilities {
    var daysOfWeeks =
        arrayOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")

    var ringerModes = arrayOf(
        Constants.RingerMode.Normal.name,
        Constants.RingerMode.Silent.name,
        Constants.RingerMode.DND.name,
        Constants.RingerMode.Vibrate.name
    )

    fun getDayString(day: Int, numberOfCharacters: Int): String? {
        return if (day < daysOfWeeks.size) if (numberOfCharacters <= daysOfWeeks[day].length) daysOfWeeks[day].substring(
            0,
            numberOfCharacters
        ) else daysOfWeeks[day] else null
    }

    fun attachSuperscriptToNumber(number: Int): String {
        val result = number.toString()
        return when (if (number < 20) number else number % 10) {
            1 -> result + "st"
            2 -> result + "nd"
            3 -> result + "rd"
            else -> result + "th"
        }
    }

    fun percentOf(value: Int, percentage: Int): Int {
        return value * percentage / 100
    }
}