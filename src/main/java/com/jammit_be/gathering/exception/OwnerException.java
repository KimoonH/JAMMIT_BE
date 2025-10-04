package com.jammit_be.gathering.exception;

public class OwnerException extends RuntimeException {

    public OwnerException(String message) {
        super(message);
    }

    // 승인 권한 없음
    public static class NoApprovalPermission extends OwnerException {
        public NoApprovalPermission() {
            super("승인 권한이 없습니다.");
        }
    }

    // 이미 승인된 참가자
    public static class AlreadyApproved extends OwnerException {
        public AlreadyApproved() {
            super("이미 승인된 참가자입니다.");
        }
    }

    // 이미 취소된 참가자
    public static class AlreadyCanceled extends OwnerException {
        public AlreadyCanceled() {
            super("이미 취소된 참가자입니다.");
        }
    }

    // 이미 거절된 참가자
    public static class AlreadyRejected extends OwnerException {
        public AlreadyRejected() {
            super("이미 거절된 참가자입니다.");
        }
    }

    // 세션 모집 인원 마감
    public static class SessionFull extends OwnerException {
        public SessionFull() {
            super("해당 세션의 모집 인원이 마감되었습니다.");
        }
    }

    // 주최자만 처리 가능
    public static class OnlyOwnerCanProcess extends OwnerException {
        public OnlyOwnerCanProcess() {
            super("모임 주최자만 처리할 수 있습니다.");
        }
    }

    // 이미 승인된 신청
    public static class AlreadyApprovedApplication extends OwnerException {
        public AlreadyApprovedApplication() {
            super("이미 승인된 신청입니다.");
        }
    }

    // 이미 취소된 신청
    public static class AlreadyCanceledApplication extends OwnerException {
        public AlreadyCanceledApplication() {
            super("이미 취소된 신청입니다.");
        }
    }

    // 이미 거절된 신청
    public static class AlreadyRejectedApplication extends OwnerException {
        public AlreadyRejectedApplication() {
            super("이미 거절된 신청입니다.");
        }
    }

    // 주최자만 완료 처리 가능
    public static class OnlyOwnerCanComplete extends OwnerException {
        public OnlyOwnerCanComplete() {
            super("모임 주최자만 완료 처리할 수 있습니다.");
        }
    }

    // 확정된 모임만 완료 처리 가능
    public static class OnlyConfirmedCanComplete extends OwnerException {
        public OnlyConfirmedCanComplete() {
            super("멤버 모집이 완료된 모임만 완료 처리할 수 있습니다.");
        }
    }
}
