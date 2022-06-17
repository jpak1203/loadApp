package com.udacity

import android.app.*
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private var selectedUrl = ""
    private var selectedFile = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            selectedUrl = getUrl(radio_group.checkedRadioButtonId)
            selectedFile = getFile(radio_group.checkedRadioButtonId)
            download()
        }

        notificationManager = getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                custom_button.buttonState = ButtonState.Completed
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.getUriForDownloadedFile(id)
                custom_button.buttonState = ButtonState.Completed
                notificationManager = getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(applicationContext, selectedFile, true))

                Toast.makeText(context, getString(R.string.download_success), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun download() {
        if (selectedUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_file_selected), Toast.LENGTH_SHORT)
                .show()
        }
        else {
            custom_button.buttonState = ButtonState.Loading

            Log.d("url", "url " + selectedUrl)
            Log.d("url", "file " + selectedFile)
            val request =
                Request(Uri.parse(selectedUrl))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)
        }
    }

    private fun getUrl(ButtonId: Int): String {
        return when (ButtonId) {
            R.id.glide_radio -> GLIDE_URL
            R.id.loadapp_radio -> LOADAPP_URL
            R.id.retrofit_radio -> RETROFIT_URL
            else -> ""
        }
    }

    private fun getFile(ButtonId: Int): String {
        return when (ButtonId) {
            R.id.glide_radio -> getString(R.string.glide_radio)
            R.id.loadapp_radio -> getString(R.string.loadapp_radio)
            R.id.retrofit_radio -> getString(R.string.retrofit_radio)
            else -> ""
        }
    }

    fun createNotification(context: Context, fileName: String, successful: Boolean): Notification? {
        val contentIntent = Intent(context, DetailActivity::class.java)

        val contentTitle = getContentTitle(context, successful)

        contentIntent.apply {
            putExtra("fileName", fileName)
            putExtra("successful", contentTitle)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(contentTitle)
            .setContentText(fileName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                context.getString(R.string.notification_button),
                pendingIntent
            )

        return builder.build()
    }

    private fun getContentTitle(context: Context, successful: Boolean): String {
        return if (successful) context.getString(R.string.download_success) else context.getString(R.string.download_failed)
    }

    companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val LOADAPP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
        const val NOTIFICATION_ID = 12345
    }

}
