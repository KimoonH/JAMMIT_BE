package com.jammit_be.gathering.constants;

public final class GatheringConstants {

    private GatheringConstants() {
    }

    public static final class ErrorMessage {
        public static final String GATHERING_NOT_FOUND = "해당 모임을 찾을 수 없습니다.";
        public static final String GATHERING_NOT_JOINABLE = "참가 신청이 불가능한 모임 상태입니다.";
        public static final String GATHERING_CLOSED = "모집이 마감된 모임입니다.";
        public static final String ALREADY_PARTICIPATED = "이미 참여한 모임입니다.";
        public static final String NOT_PARTICIPANT = "참여자가 아닌 유저입니다.";
        public static final String INVALID_SESSION = "유효하지 않은 세션입니다.";
        public static final String SESSION_FULL = "해당 세션의 모집이 마감되었습니다.";
        public static final String CANNOT_CANCEL_PAST_GATHERING = "지난 모임은 취소할 수 없습니다.";
        public static final String CANNOT_CANCEL_OWN_GATHERING = "본인이 만든 모임은 취소할 수 없습니다.";
        public static final String ALREADY_APPLIED_FOR_SESSION = "이미 해당 파트로 신청한 이력이 있습니다.";
        public static final String SESSION_RECRUITMENT_FULL = "해당 세션의 모집 인원이 이미 마감되었습니다.";
    }
}
