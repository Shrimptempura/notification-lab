package portfolio.notification_lab.provider;

public record SendResult(
        boolean success,
        boolean retryable,
        String failReason
) {

    // 발송 성공
    public static SendResult sent() {
        return new SendResult(true, false, null);
    }

    // 재시도 가능한 실패
    public static SendResult retryableFailure(String failReason) {
        validateFailReason(failReason);
        return new SendResult(false, true, failReason);
    }

    // 재시도 불가능한 실패
    public static SendResult nonRetryableFailure(String failReason) {
        validateFailReason(failReason);
        return new SendResult(false, false, failReason);
    }

    private static void validateFailReason(String failReason) {
        if (failReason == null || failReason.isBlank()) {
            throw new IllegalArgumentException("failReason은 비어 있을 수 없습니다.");
        }
    }

}
