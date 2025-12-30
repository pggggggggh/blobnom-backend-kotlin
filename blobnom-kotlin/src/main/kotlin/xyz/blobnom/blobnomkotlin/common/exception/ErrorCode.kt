package xyz.blobnom.blobnomkotlin.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
) {
    // 401
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "아이디나 비밀번호를 확인해주세요."),


    // 400
    TOO_LESS_MISSIONS(HttpStatus.BAD_REQUEST, "TOO_LESS_MISSIONS", "쿼리에 해당하는 문제 수가 너무 적습니다."),
    TOO_FAST_STARTSAT(HttpStatus.BAD_REQUEST, "TOO_FAST_STARTSAT", "방의 시작 시간이 너무 이릅니다."),
    ROOM_FULL(HttpStatus.BAD_REQUEST, "ROOM_FULL", "방이 가득 찼습니다."),
    UNSOLVABLE_PROBLEM(HttpStatus.BAD_REQUEST, "UNSOLVABLE_PROBLEM", "해결할 수 없는 문제입니다."),
    MISSION_VERIFICATION_FAILED(
        HttpStatus.BAD_REQUEST,
        "MISSION_VERIFIACTION_FAILED",
        "문제 해결 검증에 실패했습니다. 만약 해당 문제를 '맞았습니다!!' 판정을 받은 다음에도 이 오류가 보인다면, 잠시 뒤 다시 시도해주세요."
    ),
    ROOM_ENDED(HttpStatus.BAD_REQUEST, "ROOM_ENDED", "종료된 방입니다."),
    JOIN_DEADLINE_EXCEEDED(HttpStatus.BAD_REQUEST, "JOIN_DEADLINE_EXCEEDED", "시작 준비 중인 방입니다. 시작 이후 다시 시도해주세요."),
    FAILED_TOKEN_VERIFICATION(HttpStatus.BAD_REQUEST, "FAILED_TOKEN_VERIFICATION", "토큰 인증에 실패했습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    UNLINKED_PLATFORM(HttpStatus.BAD_REQUEST, "UNLINKED_PLATFORM", "계정이 해당 플랫폼(BOJ, 코드포스)과의 연동되지 않아 진행할 수 없습니다."),

    // 404
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "존재하지 않는 회원입니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "존재하지 않는 방입니다."),
    PLATFORM_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "PLATFORM_USER_NOT_FOUND", "존재하지 않는 계정입니다."),

    // 409
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "ALREADY_PARTICIPATED", "이미 참가한 방입니다."),
    ALREADY_SOLVED(HttpStatus.CONFLICT, "ALREADY_SOLVED", "이미 해결된 문제입니다."),
    ALREADY_TAKEN(HttpStatus.CONFLICT, "ALREADY_TAKEN", "이미 사용 중인 핸들/이메일입니다."),

    // 503
    EXTERNAL_API_TIMEOUT(
        HttpStatus.SERVICE_UNAVAILABLE,
        "EXTERNAL_API_TIMEOUT",
        "외부 서비스 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."
    ),
}