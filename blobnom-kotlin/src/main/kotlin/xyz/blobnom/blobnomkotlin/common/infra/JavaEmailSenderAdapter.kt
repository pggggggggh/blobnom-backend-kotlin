package xyz.blobnom.blobnomkotlin.common.infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.app.EmailSenderPort

@Component
class JavaEmailSenderAdapter(
    private val mailSender: JavaMailSender
) : EmailSenderPort {
    override suspend fun sendEmail(email: String, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(email)
            helper.setSubject(subject)
            helper.setText(body, true)

            try {
                mailSender.send(message)
            } catch (e: Exception) {
                throw RuntimeException("Mail send failed : ${e.message}")
            }
        }
    }
}