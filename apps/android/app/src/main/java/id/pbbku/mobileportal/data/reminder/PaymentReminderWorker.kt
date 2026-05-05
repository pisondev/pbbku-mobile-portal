package id.pbbku.mobileportal.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.pbbku.mobileportal.MainActivity
import id.pbbku.mobileportal.R

class PaymentReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val taxYear = inputData.getInt(KEY_TAX_YEAR, 0)
        val offsetDays = inputData.getInt(KEY_OFFSET_DAYS, 0)
        val title = "Pengingat PBB jatuh tempo"
        val text = if (offsetDays > 0) {
            "Tagihan PBB tahun $taxYear mendekati jatuh tempo dalam $offsetDays hari."
        } else {
            "Tagihan PBB tahun $taxYear mendekati jatuh tempo."
        }

        createChannel()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            inputData.getInt(KEY_NOTIFICATION_ID, taxYear),
            notification,
        )
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pengingat PBB",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Pengingat lokal jatuh tempo tagihan PBB."
        }
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "pbbku_payment_reminders"
        const val KEY_TAX_YEAR = "tax_year"
        const val KEY_OFFSET_DAYS = "offset_days"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
