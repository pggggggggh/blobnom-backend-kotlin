package xyz.blobnom.blobnomkotlin.member.domain

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TemporaryPasswordGenerator {
    fun generate(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }
}