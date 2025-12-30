package xyz.blobnom.blobnomkotlin.member.app

import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary

fun Member.toMemberSummary(): MemberSummary {
    return MemberSummary(
        id = id!!,
        handle = handle,
        role = role,
        rating = rating,
        accounts = platformUsers.associate { it.platform to it.handle }
    )
}