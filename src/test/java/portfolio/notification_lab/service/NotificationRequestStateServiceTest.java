package portfolio.notification_lab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.command.RequestDeadCommand;
import portfolio.notification_lab.command.RequestFailureCommand;
import portfolio.notification_lab.mapper.NotificationRequestStateMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRequestStateServiceTest {

    @Mock
    private NotificationRequestStateMapper mapper;

    @InjectMocks
    private NotificationRequestStateServiceImpl service;

    @Nested
    @DisplayName("선점 상태 전이")
    class Reservation {

        @Test
        @DisplayName("PENDING 요청을 RESERVED로 선점한다")
        void reserve_success() {
            Long requestId = 1L;
            when(mapper.markPendingAsReserved(requestId)).thenReturn(1);
            service.reserve(requestId);

            verify(mapper).markPendingAsReserved(requestId);
        }

        @Test
        @DisplayName("선점 대상이 아니면 예외 발생")
        void reserve_fail_whenUpdateCountIsZero() {
            Long requestId = 1L;
            when(mapper.markPendingAsReserved(requestId)).thenReturn(0);

            assertThatThrownBy(() -> service.reserve(requestId))
                    .isInstanceOf(IllegalStateException.class);

            verify(mapper).markPendingAsReserved(requestId);
        }
    }

    @Nested
    @DisplayName("발송 결과 상태 전이")
    class SendResult {

        @Test
        @DisplayName("RESERVED 요청을 SENT로 처리한다")
        void markSent_success() {
            Long requestId = 1L;
            when(mapper.markReservedAsSent(requestId)).thenReturn(1);
            service.markSent(requestId);

            verify(mapper).markReservedAsSent(requestId);
        }

        @Test
        @DisplayName("SENT 처리 대상이 아니면 예외 발생")
        void markSent_fail_whenUpdateCountIsZero() {
            Long requestId = 1L;
            when(mapper.markReservedAsSent(requestId)).thenReturn(0);

            assertThatThrownBy(() -> service.markSent(requestId))
                    .isInstanceOf(IllegalStateException.class);

            verify(mapper).markReservedAsSent(requestId);
        }

        @Test
        @DisplayName("RESERVED 요청을 FAILED로 처리하고 재시도 시간을 설정한다")
        void markRetryableFailure_success() {
            Long requestId = 1L;
            String failReason = "TEMPORARY_ERROR";
            int nextRetrySeconds = 30;

            when(mapper.markReservedAsFailed(any(RequestFailureCommand.class))).thenReturn(1);
            service.markRetryableFailure(requestId, failReason, nextRetrySeconds);

            // Command 객체 내부 값 확인이 핵심 -> ArgumentCaptor
            ArgumentCaptor<RequestFailureCommand> captor = ArgumentCaptor.forClass(RequestFailureCommand.class);

            verify(mapper).markReservedAsFailed(captor.capture());

            RequestFailureCommand command = captor.getValue();
            assertThat(command.getRequestId()).isEqualTo(requestId);
            assertThat(command.getFailReason()).isEqualTo(failReason);
            assertThat(command.getNextRetrySeconds()).isEqualTo(nextRetrySeconds);
        }

        @Test
        @DisplayName("FAILED 처리 대상이 아니면 예외 발생")
        void markRetryableFailure_fail_whenUpdateCountIsZero() {
            Long requestId = 1L;
            String failReason = "TEMPORARY_ERROR";
            int nextRetrySeconds = 30;

            when(mapper.markReservedAsFailed(any(RequestFailureCommand.class))).thenReturn(0);

            assertThatThrownBy(() -> service.markRetryableFailure(requestId, failReason, nextRetrySeconds))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("재시도 불가능 실패는 DEAD 처리한다")
        void markNonRetryableFailure_success() {
            Long requestId = 1L;
            String failReason = "PERMANENT_ERROR";

            when(mapper.markReservedAsDead(any(RequestDeadCommand.class))).thenReturn(1);

            service.markDeadByNonRetryableFailure(requestId, failReason);

            ArgumentCaptor<RequestDeadCommand> captor = ArgumentCaptor.forClass(RequestDeadCommand.class);

            verify(mapper).markReservedAsDead(captor.capture());

            RequestDeadCommand command = captor.getValue();
            assertThat(command.getRequestId()).isEqualTo(requestId);
            assertThat(command.getFailReason()).isEqualTo(failReason);
        }
    }

    @Nested
    @DisplayName("재시도 상태 전이")
    class Retry {

        @Test
        @DisplayName("FAILED 요청을 재시도 대기 상태로 전환한다(PENDING)")
        void markReadyForRetry_success() {
            Long requestId = 1L;

            when(mapper.markFailedAsPending(requestId)).thenReturn(1);
            service.markReadyForRetry(requestId);

            verify(mapper).markFailedAsPending(requestId);
        }

        @Test
        @DisplayName("재시도 조건을 만족하지 않으면 예외가 발생")
        void markReadyForRetry_fail_whenUpdateCountIsZero() {
            Long requestId = 1L;

            when(mapper.markFailedAsPending(requestId)).thenReturn(0);

            assertThatThrownBy(() -> service.markReadyForRetry(requestId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("최대 재시도 횟수에 도달한 FAILED 요청을 DEAD 처리 한다")
        void markDeadAfterRetryExceeded_success() {
            Long requestId = 1L;
            String failReason = "MAX_RETRY_EXCEEDED";

            when(mapper.markFailedAsDead(any(RequestDeadCommand.class))).thenReturn(1);
            service.markDeadAfterRetryExceeded(requestId, failReason);

            ArgumentCaptor<RequestDeadCommand> captor = ArgumentCaptor.forClass(RequestDeadCommand.class);

            verify(mapper).markFailedAsDead(captor.capture());

            RequestDeadCommand command = captor.getValue();
            assertThat(command.getRequestId()).isEqualTo(requestId);
            assertThat(command.getFailReason()).isEqualTo(failReason);
        }

        @Test
        @DisplayName("DEAD 처리 조건을 만족하지 않으면 예외 발생")
        void markDeadAfterRetryExceeded_fail_whenUpdateCountIsZero() {
            Long requestId = 1L;
            String failReason = "MAX_RETRY_EXCEEDED";

            when(mapper.markFailedAsDead(any(RequestDeadCommand.class))).thenReturn(0);

            assertThatThrownBy(() -> service.markDeadAfterRetryExceeded(requestId, failReason))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("worker 장애 복구")
    class Recovery {

        @Test
        @DisplayName("시간 만료된 RESERVED 요청을 PENDING으로 복구한다")
        void releaseExpiredReservations_success() {
            int timeoutMinutes = 10;

            when(mapper.releaseExpiredReservations(timeoutMinutes)).thenReturn(2);

            int releasedCount = service.releaseExpiredReservations(timeoutMinutes);

            assertThat(releasedCount).isEqualTo(2);
            verify(mapper).releaseExpiredReservations(timeoutMinutes);
        }
    }

    @Nested
    @DisplayName("입력값 검증")
    class Validation {

        @Test
        @DisplayName("requestId가 유효값이 아니면 예외 발생하고 mapper 호출X")
        void fail_whenInvalidRequestId() {
            assertThatThrownBy(() -> service.reserve(null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> service.reserve(0L))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("failReason이 null이면 예외 발생하고 mapper 호출X")
        void fail_whenBlankFailReason() {
            assertThatThrownBy(() -> service.markRetryableFailure(1L, null, 30))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("nextRetrySeconds가 1 미만이면 예외 발생하고 mapper 호출X")
        void fail_whenInvalidNextRetrySeconds() {
            assertThatThrownBy(() -> service.markRetryableFailure(1L, "TEMPORARY_ERROR", 0))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("timeoutMinutes가 1 미만이면 예외 발생하고 mapper 호출X")
        void fail_whenInvalidTimeoutMinutes() {
            assertThatThrownBy(() -> service.releaseExpiredReservations(0))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(mapper);
        }
    }
}