package com.example.votree.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PermissionManager(private val activity: FragmentActivity) {
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private var isStorageImagePermitted: Boolean = false
    private var isStorageVideoPermitted: Boolean = false
    private var isStorageAudioPermitted: Boolean = false
    private var isNotificationPermitted: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermissions() {
        if (allPermissionsGranted()) {
            // All permissions are granted
        } else {
            requestPermissionStorageImages()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionStorageImages() {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                requiredPermissions[0]
            ) == PackageManager.PERMISSION_GRANTED -> {
                isStorageImagePermitted = true
                if (!allPermissionsGranted()) {
                    requestPermissionStorageVideos()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                requiredPermissions[0]
            ) -> {
//                sendToSettingDialog()
            }

            else -> {
                requestPermissionLauncherStorageImages.launch(requiredPermissions[0])
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val requestPermissionLauncherStorageImages =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isStorageImagePermitted = true
                if (!allPermissionsGranted()) {
                    requestPermissionStorageVideos()
                }
            } else {
                isStorageImagePermitted = false
//                sendToSettingDialog()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionStorageVideos() {
        // Similar implementation as requestPermissionStorageImages
        when {
            ContextCompat.checkSelfPermission(
                activity,
                requiredPermissions[1]
            ) == PackageManager.PERMISSION_GRANTED -> {
                isStorageVideoPermitted = true
                if (!allPermissionsGranted()) {
                    requestPermissionStorageAudios()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                requiredPermissions[1]
            ) -> {
//                sendToSettingDialog()
            }

            else -> {
                requestPermissionLauncherStorageVideos.launch(requiredPermissions[1])
            }
        }
    }

    // Implement requestPermissionLauncherStorageVideos and requestPermissionStorageAudios in similar manner
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val requestPermissionLauncherStorageVideos =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isStorageVideoPermitted = true
                if (!allPermissionsGranted()) {
                    requestPermissionStorageAudios()
                }
            } else {
                isStorageVideoPermitted = false
//                sendToSettingDialog()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionStorageAudios() {
        // Similar implementation as requestPermissionStorageImages
        when {
            ContextCompat.checkSelfPermission(
                activity,
                requiredPermissions[2]
            ) == PackageManager.PERMISSION_GRANTED -> {
                isStorageAudioPermitted = true
                if (!allPermissionsGranted()) {
                    // All permissions are granted
                    requestPermissionNotifications()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                requiredPermissions[2]
            ) -> {
//                sendToSettingDialog()
            }

            else -> {
                requestPermissionLauncherStorageAudios.launch(requiredPermissions[2])
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val requestPermissionLauncherStorageAudios =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isStorageAudioPermitted = true
                if (!allPermissionsGranted()) {
                    requestPermissionStorageAudios()
                }
            } else {
                isStorageAudioPermitted = false
//                sendToSettingDialog()
            }
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionNotifications() {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                requiredPermissions[3]
            ) == PackageManager.PERMISSION_GRANTED -> {
                isNotificationPermitted = true
                if (!allPermissionsGranted()) {
                    sendToSettingDialog()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                requiredPermissions[3]
            ) -> {
                
            }

            else -> {
                requestPermissionLauncherNotifications.launch(requiredPermissions[3])
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val requestPermissionLauncherNotifications =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isNotificationPermitted = true
                if (!allPermissionsGranted()) {
                    // All permissions are granted
                    sendToSettingDialog()
                }
            } else {
                isNotificationPermitted = false
//                sendToSettingDialog()
            }
        }

    fun allPermissionsGranted(): Boolean {
        return isStorageImagePermitted && isStorageVideoPermitted && isStorageAudioPermitted && isNotificationPermitted
    }

    private fun sendToSettingDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("Please allow the required permissions to proceed")
            .setPositiveButton("Go to settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
