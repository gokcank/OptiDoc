package com.gokcank.optidoc.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.gokcank.optidoc.BuildConfig
import com.gokcank.optidoc.R
import java.io.File

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.about), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.about_developer),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // GitHub Bağlantısı
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gokcank/OptiDoc"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "GitHub")
                }

                // Hata Bildirimi (Logcat Ekli)
                Button(
                    onClick = { sendBugReportEmail(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.bug_report))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun sendBugReportEmail(context: Context) {
    val email = "destek.gokcank@gmail.com"
    val subject = context.getString(R.string.bug_report)
    val body = context.getString(
        R.string.email_body_template,
        Build.MODEL,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        BuildConfig.VERSION_NAME
    )

    // Logcat çıktısını al ve önbellek dizinine kaydet
    val logFile = try {
        val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time", "-t", "300", "*:V"))
        val logContent = process.inputStream.bufferedReader().use { it.readText() }
        val file = File(context.cacheDir, "optidoc_logs.txt")
        file.writeText(logContent)
        file
    } catch (e: Exception) {
        null
    }

    val intent = if (logFile != null && logFile.exists() && logFile.length() > 0) {
        val logUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            logFile
        )
        Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, logUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }

    try {
        context.startActivity(Intent.createChooser(intent, subject))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
