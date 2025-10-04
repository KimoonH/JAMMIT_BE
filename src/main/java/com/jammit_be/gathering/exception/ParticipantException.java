package com.jammit_be.gathering.exception;

public class ParticipantException extends RuntimeException {
    public ParticipantException(String message) {
        super(message);
    }

    // 참가 신청을 찾을 수 없음
    public static class NotFound extends ParticipantException {
        public NotFound() {
            super("해당 참가 신청이 없습니다.");
        }
    }

    // 해당 모임의 참가자가 아님
    public static class NotGatheringParticipant extends ParticipantException {
        public NotGatheringParticipant() {
            super("해당 모임의 참가자가 아닙니다.");
        }
    }

    // 본인만 취소 가능
    public static class OnlySelfCanCancel extends ParticipantException {
        public OnlySelfCanCancel() {
            super("본인의 참가 신청만 취소할 수 있습니다.");
        }
    }

    // 이미 취소됨
    public static class AlreadyCanceled extends ParticipantException {
        public AlreadyCanceled() {
            super("이미 취소된 참가 신청입니다.");
        }
    }

    // 이미 참여 완료됨
    public static class AlreadyCompleted extends ParticipantException {
        public AlreadyCompleted() {
            super("이미 참여 완료된 모임은 취소할 수 없습니다.");
        }
    }

    // 이미 해당 세션에 신청함
    public static class AlreadyAppliedForSession extends ParticipantException {
        public AlreadyAppliedForSession() {
            super("이미 해당 파트로 신청한 이력이 있습니다.");
        }
    }

    // 세션 모집 인원 마감
    public static class SessionRecruitmentFull extends ParticipantException {
        public SessionRecruitmentFull() {
            super("해당 세션의 모집 인원이 이미 마감되었습니다.");
        }
    }
}
