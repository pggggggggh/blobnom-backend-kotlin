package xyz.blobnom.blobnomkotlin.common.app

interface EmailSenderPort {
    suspend fun sendEmail(email: String, subject: String, body: String)
}