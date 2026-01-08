package xyz.blobnom.blobnomkotlin.member.app

import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import xyz.blobnom.blobnomkotlin.member.dto.PlatformAccountInfo

fun Member.toMemberSummary(): MemberSummary {
    return MemberSummary(
        id = id!!,
        handle = handle,
        role = role,
        rating = rating,
        accounts = platformUsers.map { PlatformAccountInfo(it.platform, it.handle) }
    )
}