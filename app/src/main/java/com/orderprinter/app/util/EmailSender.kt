package com.orderprinter.app.util

import android.content.Context
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Αποστολή email μέσω SMTP.
 * Χρησιμοποιεί τις ρυθμίσεις που έχει αποθηκεύσει ο χρήστης.
 */
object EmailSender {

    data class SmtpConfig(
        val senderEmail: String,
        val senderPassword: String,
        val smtpHost: String,
        val smtpPort: String,
        val printerEmail: String
    )

    fun getConfig(context: Context): SmtpConfig? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val senderEmail = prefs.getString("sender_email", "") ?: ""
        val senderPassword = prefs.getString("sender_password", "") ?: ""
        val smtpHost = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com"
        val smtpPort = prefs.getString("smtp_port", "587") ?: "587"
        val printerEmail = prefs.getString("printer_email", "") ?: ""

        if (senderEmail.isBlank() || senderPassword.isBlank() || printerEmail.isBlank()) {
            return null
        }
        return SmtpConfig(senderEmail, senderPassword, smtpHost, smtpPort, printerEmail)
    }

    /**
     * Αποστολή email σε background thread.
     * @return true αν η αποστολή πέτυχε
     */
    suspend fun sendOrder(config: SmtpConfig, subject: String, body: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", config.smtpHost)
                    put("mail.smtp.port", config.smtpPort)
                    put("mail.smtp.ssl.trust", config.smtpHost)
                    put("mail.smtp.connectiontimeout", "10000")
                    put("mail.smtp.timeout", "10000")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(config.senderEmail, config.senderPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(config.senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.printerEmail))
                    setSubject(subject, "UTF-8")
                    setText(body, "UTF-8")
                }

                Transport.send(message)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
