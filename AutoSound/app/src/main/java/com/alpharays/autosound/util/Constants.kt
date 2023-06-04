package com.alpharays.autosound.util

object Constants {
    const val ADD_TRIGGER_ACTIVITY_RC = 1111
    const val RINGER_MODE_BUNDLE_NAME = "RINGER_MODE"
    const val VOLUMES_BUNDLE_NAME = "VOLUMES"
    const val LOCATION_PERMISSION_REQUEST_CODE = 5

    enum class RingerMode(val value: Int) {
        DND(4), Normal(2), Vibrate(1), Silent(0);
    }
}