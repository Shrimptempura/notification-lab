drop table notification_request;
truncate table notification_request;

-- id 시퀀스까지 초기화
TRUNCATE TABLE notification_request RESTART IDENTITY;

-- 대용량 테스트 1,000,000건
INSERT INTO notification_request (
    campaign_id,
    recipient_id,
    status,
    payload,
    retry_count
)
SELECT
    1 AS campaign_id,
    gs AS recipient_id,
    'PENDING' AS status,
    'test 1,000,000 payload' AS payload,
    0 AS retry_count
FROM generate_series(1, 1000000) AS gs;