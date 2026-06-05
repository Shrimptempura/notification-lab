package portfolio.notification_lab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.config.NotificationRetryProperties;
import portfolio.notification_lab.domain.notification.NotificationStatus;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.provider.NotificationProvider;
import portfolio.notification_lab.provider.SendResult;
import portfolio.notification_lab.recorder.SendAttemptRecorder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSendServiceTest {

    @Mock
    private NotificationProvider provider;

    @Mock
    private NotificationRequestStateService stateService;

    @Mock
    private NotificationRetryProperties retryProperties;

    @Mock
    private SendAttemptRecorder sendAttemptRecorder;

    @InjectMocks
    private NotificationSendServiceImpl service;

    @Nested
    @DisplayName("알림 1건 발송 처리")
    class SendOne {

        @Test
        @DisplayName("발송 성공이면 SENT 처리")
        void sendOne_success() {
            NotificationRequestDto request = createRequest(1L, 100L);
            SendResult result = SendResult.sent();

            when(provider.send(request)).thenReturn(result);

            service.sendOne(request);

            verify(provider).send(request);

            InOrder inOrder = inOrder(stateService, sendAttemptRecorder);
            inOrder.verify(stateService).markSent(request.getId());
            inOrder.verify(sendAttemptRecorder).record(request, result, NotificationStatus.SENT);

            verify(stateService, never()).markRetryableFailure(anyLong(), anyString(), anyInt(), anyInt());
            verify(stateService, never()).markDeadByNonRetryableFailure(anyLong(), anyString());
        }

        @Test
        @DisplayName("재시도 가능한 실패이면 FAILED 처리")
        void sendOne_retryableFailure() {
            NotificationRequestDto request = createRequest(1L, 100L);
            SendResult result = SendResult.retryableFailure("TIMED_OUT");

            when(provider.send(request)).thenReturn(result);
            when(retryProperties.nextRetrySeconds()).thenReturn(60);
            when(retryProperties.maxRetryCount()).thenReturn(3);

            service.sendOne(request);

            verify(provider).send(request);

            InOrder inOrder = inOrder(stateService, sendAttemptRecorder);
            inOrder.verify(stateService).markRetryableFailure(request.getId(), "TIMED_OUT", 60, 3);
            inOrder.verify(sendAttemptRecorder).record(request, result, NotificationStatus.FAILED);

            verify(stateService, never()).markSent(anyLong());
            verify(stateService, never()).markDeadByNonRetryableFailure(anyLong(), anyString());
        }

        @Test
        @DisplayName("재시도 불가능한 실패이면 DEAD 처리")
        void sendOne_nonRetryableFailure() {
            NotificationRequestDto request = createRequest(1L, 100L);
            SendResult result = SendResult.nonRetryableFailure("INVALID_RECIPIENT");

            when(provider.send(request)).thenReturn(result);

            service.sendOne(request);

            verify(provider).send(request);

            InOrder inOrder = inOrder(stateService, sendAttemptRecorder);
            inOrder.verify(stateService).markDeadByNonRetryableFailure(request.getId(), "INVALID_RECIPIENT");
            inOrder.verify(sendAttemptRecorder).record(request, result, NotificationStatus.DEAD);

            verify(stateService, never()).markSent(anyLong());
            verify(stateService, never()).markRetryableFailure(anyLong(), anyString(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("입력값 검증")
    class Validation {

        @Test
        @DisplayName("request가 null이면 예외 발생하고 provider 호출 X")
        void sendOne_nullRequest() {
            assertThatThrownBy(() -> service.sendOne(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("request는 null일 수 없습니다.");

            verifyNoInteractions(provider);
            verifyNoInteractions(stateService);
        }

        @Test
        @DisplayName("requestId가 null 또는 0 이하이면 예외 발생하고 provider 호출 X")
        void sendOne_nullRequestId() {
            NotificationRequestDto requestIdIsNull = createRequest(null, 100L);
            NotificationRequestDto requestIdIsZero = createRequest(0L, 200L);

            assertThatThrownBy(() -> service.sendOne(requestIdIsNull))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requestId가 유효하지 않습니다");

            assertThatThrownBy(() -> service.sendOne(requestIdIsZero))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requestId가 유효하지 않습니다");

            verifyNoInteractions(provider);
            verifyNoInteractions(stateService);
        }
    }

    private NotificationRequestDto createRequest(Long requestId, Long recipientId) {
        NotificationRequestDto request = new NotificationRequestDto();
        request.setId(requestId);
        request.setRecipientId(recipientId);

        return request;
    }
}