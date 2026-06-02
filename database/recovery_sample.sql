-- recovery test
-- properties 현재 timeout minute 5분

-- ex 10분전 조건 통과, 1분전 조건 불충족
INSERT INTO notification_request (
    campaign_id,
    recipient_id,
    status,
    payload,
    reserved_at,
    created_at,
    updated_at
)
VALUES (
        1,
        999998,
        'RESERVED',
        'recovery test',
        NOW() - INTERVAL '1 minute',
        NOW(),
        NOW()
);

