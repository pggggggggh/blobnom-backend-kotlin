package xyz.blobnom.blobnomkotlin.member.domain.repository

import org.springframework.data.repository.CrudRepository
import xyz.blobnom.blobnomkotlin.member.domain.PasswordResetToken

interface PasswordResetTokenRepository : CrudRepository<PasswordResetToken, String> {
}