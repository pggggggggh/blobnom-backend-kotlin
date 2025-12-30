package xyz.blobnom.blobnomkotlin.auth.app.port

interface SecurityPort {
    fun authenticate(handle: String, password: String): String
    fun hashPassword(rawPassword: String): String
    fun matchPassword(rawPassword: String, hashedPassword: String): Boolean
}