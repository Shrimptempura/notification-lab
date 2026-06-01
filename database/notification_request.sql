CREATE TABLE notification_request
(
    id            BIGSERIAL PRIMARY KEY,
    campaign_id   BIGINT      NOT null, -- 이벤트 번호 ex)001: 기본 알림, 002: 주말 할인, 003: 봄맞이 알림
    recipient_id  BIGINT      NOT NULL, -- 발송인

    status        VARCHAR(30) NOT NULL, -- 상태
    payload       TEXT        NOT NULL, -- 발송 메시지

    retry_count   INT         NOT NULL DEFAULT 0,

    fail_reason   TEXT,
    next_retry_at TIMESTAMP,

    reserved_at   TIMESTAMP,
    sent_at       TIMESTAMP,
    failed_at     TIMESTAMP,
    dead_at       TIMESTAMP,

    created_at    TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT now()
);