package com.jammit_be.gathering.exception;

public class GatheringException extends RuntimeException{

    public GatheringException(String message) {
        super(message);
    }
    // 모임을 찾을 수 없음
    public static class NotFound extends GatheringException {
        public NotFound() {
            super("해당 모임을 찾을 수 없습니다.");
        }
    }

    // 참가 불가능한 상태
    public static class NotJoinable extends GatheringException {
        public NotJoinable() {
            super("참가 신청이 불가능한 모임 상태입니다.");
        }
    }

    // 수정 권한 없음
    public static class NoUpdatePermission extends GatheringException {
        public NoUpdatePermission() {
            super("수정 권한이 없습니다.");
        }
    }

    // 취소 권한 없음
    public static class NoCancelPermission extends GatheringException {
        public NoCancelPermission() {
            super("취소 권한이 없습니다.");
        }
    }
}
