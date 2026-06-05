package portfolio.notification_lab.recorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.command.SendAttemptCommand;
import portfolio.notification_lab.domain.notification.NotificationStatus;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.mapper.NotificationSendAttemptMapper;
import portfolio.notification_lab.provider.SendResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendAttemptRecorderTest {

    @Mock
    private NotificationSendAttemptMapper sendAttemptMapper;

    @InjectMocks
    private SendAttemptRecorderImpl recorder;

    @Nested
    @DisplayName("발송 시도 이력 기록")
    class Record {

        @Test
        @DisplayName("발송 성공이면 SENT 저장")
        void record_success() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 0);
            SendResult result = SendResult.sent();

            when(sendAttemptMapper.insertSendAttempt(any(SendAttemptCommand.class))).thenReturn(1);

            recorder.record(request, result, NotificationStatus.SENT);

            ArgumentCaptor<SendAttemptCommand> captor = ArgumentCaptor.forClass(SendAttemptCommand.class);

            verify(sendAttemptMapper).insertSendAttempt(captor.capture());

            SendAttemptCommand command = captor.getValue();

            assertThat(command.getRequestId()).isEqualTo(request.getId());
            assertThat(command.getCampaignId()).isEqualTo(request.getCampaignId());
            assertThat(command.getRecipientId()).isEqualTo(request.getRecipientId());
            assertThat(command.getAttemptNo()).isEqualTo(1);        // retryCount + 1
            assertThat(command.getResultStatus()).isEqualTo(NotificationStatus.SENT.name());
            assertThat(command.getProviderResultType()).isEqualTo("SENT");
            assertThat(command.getFailReason()).isNull();
        }

        @Test
        @DisplayName("재시도 가능한 실패인 경우 발송 기록 저장")
        void record_retryableFailure() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 1);
            SendResult result = SendResult.retryableFailure("TIMED_OUT");

            when(sendAttemptMapper.insertSendAttempt(any(SendAttemptCommand.class))).thenReturn(1);

            recorder.record(request, result, NotificationStatus.FAILED);

            ArgumentCaptor<SendAttemptCommand> captor = ArgumentCaptor.forClass(SendAttemptCommand.class);

            verify(sendAttemptMapper).insertSendAttempt(captor.capture());

            SendAttemptCommand command = captor.getValue();

            assertThat(command.getAttemptNo()).isEqualTo(2);        // retryCount + 1
            assertThat(command.getResultStatus()).isEqualTo(NotificationStatus.FAILED.name());
            assertThat(command.getProviderResultType()).isEqualTo("RETRYABLE_FAILURE");
            assertThat(command.getFailReason()).isEqualTo("TIMED_OUT");
        }

        @Test
        @DisplayName("재시도 가능한 실패가 최종 DEAD인 경우 최종 상태 DEAD와 RETRYABLE_FAIURE 발송 기록 저장")
        void record_retryableFailure_finalDead() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 2);
            SendResult result = SendResult.retryableFailure("TIMED_OUT");

            when(sendAttemptMapper.insertSendAttempt(any(SendAttemptCommand.class))).thenReturn(1);

            recorder.record(request, result, NotificationStatus.DEAD);

            ArgumentCaptor<SendAttemptCommand> captor = ArgumentCaptor.forClass(SendAttemptCommand.class);

            verify(sendAttemptMapper).insertSendAttempt(captor.capture());

            SendAttemptCommand command = captor.getValue();

            assertThat(command.getAttemptNo()).isEqualTo(3);        // retryCount + 1
            assertThat(command.getResultStatus()).isEqualTo(NotificationStatus.DEAD.name());
            assertThat(command.getProviderResultType()).isEqualTo("RETRYABLE_FAILURE");
            assertThat(command.getFailReason()).isEqualTo("TIMED_OUT");
        }

        @Test
        @DisplayName("재시도 불가능 실패이면 최종 DEAD와 NON_RETRYABLE_FAILURE 저장")
        void record_nonRetryableFailure() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 0);
            SendResult result = SendResult.nonRetryableFailure("INVALID_RECIPIENT");

            when(sendAttemptMapper.insertSendAttempt(any(SendAttemptCommand.class))).thenReturn(1);

            recorder.record(request, result, NotificationStatus.DEAD);

            ArgumentCaptor<SendAttemptCommand> captor = ArgumentCaptor.forClass(SendAttemptCommand.class);

            verify(sendAttemptMapper).insertSendAttempt(captor.capture());

            SendAttemptCommand command = captor.getValue();

            assertThat(command.getAttemptNo()).isEqualTo(1);        // retryCount + 1
            assertThat(command.getResultStatus()).isEqualTo(NotificationStatus.DEAD.name());
            assertThat(command.getProviderResultType()).isEqualTo("NON_RETRYABLE_FAILURE");
            assertThat(command.getFailReason()).isEqualTo("INVALID_RECIPIENT");
        }

        @Test
        @DisplayName("발송 시도 이력 저장 결과가 1건이 아니면 예외 발생")
        void record_insertFail() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 0);
            SendResult result = SendResult.sent();

            when(sendAttemptMapper.insertSendAttempt(any(SendAttemptCommand.class))).thenReturn(0);

            assertThatThrownBy(() -> recorder.record(request, result, NotificationStatus.SENT))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("발송 시도 이력 저장 실패");
        }
    }

    @Nested
    @DisplayName("입력값 검증")
    class Validation {

        @Test
        @DisplayName("request가 null이면 예외 발생하고 mapper 호출 X")
        void record_nullRequest() {
            assertThatThrownBy(() -> recorder.record(null, SendResult.sent(), NotificationStatus.SENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("발송 정보는 필수입니다.");

            verifyNoInteractions(sendAttemptMapper);
        }

        @Test
        @DisplayName("발송 결과가 null이면 예외 발생하고 mapper 호출X")
        void record_nullResult() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 0);

            assertThatThrownBy(() -> recorder.record(request, null, NotificationStatus.SENT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("발송 결과는 필수입니다.");

            verifyNoInteractions(sendAttemptMapper);
        }

        @Test
        @DisplayName("최종 상태가 null이면 예외 발생하고 mapper 호출 X")
        void record_nullFinalStatus() {
            NotificationRequestDto request = createRequest(1L, 100L, 200L, 0);

            assertThatThrownBy(() -> recorder.record(request, SendResult.sent(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최종 발송 상태는 필수입니다.");

            verifyNoInteractions(sendAttemptMapper);
        }
    }


    private NotificationRequestDto createRequest(Long requestId, Long campaignId, Long recipientId, int retryCount) {
        NotificationRequestDto request = new NotificationRequestDto();
        request.setId(requestId);
        request.setCampaignId(campaignId);
        request.setRecipientId(recipientId);
        request.setRetryCount(retryCount);

        return request;
    }



}