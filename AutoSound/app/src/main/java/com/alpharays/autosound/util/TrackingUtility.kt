package com.alpharays.autosound.util

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object TrackingUtility {

    fun hasDoNotDisturbPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
        )
}