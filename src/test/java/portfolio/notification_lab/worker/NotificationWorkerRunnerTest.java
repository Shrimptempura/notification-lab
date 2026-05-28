package portfolio.notification_lab.worker;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.service.NotificationSendService;
import portfolio.notification_lab.service.NotificationWorkerService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerRunnerTest {

    @Mock
    private NotificationWorkerService workerService;

    @Mock
    private NotificationSendService sendService;

    @InjectMocks
    private NotificationWorkerRunner runner;

    @Nested
    @DisplayName("worker 1회 실행")
    class RunOnce {

        @Test
        @DisplayName("선점된 요청이 없으면 종료")
        void runOnce_emptyRequests() {
            when(workerService.reserveRunnableRequests(10, 3)).thenReturn(List.of());

            int result = runner.runOnce(10, 3);

            assertThat(result).isEqualTo(0);

            verify(workerService).reserveRunnableRequests(10, 3);
            verifyNoInteractions(sendService);
        }

        @Test
        @DisplayName("선점된 요청들을 처리한다")
        void runOnce_success() {
            NotificationRequestDto request1 = createRequest(1L);
            NotificationRequestDto request2 = createRequest(2L);

            List<NotificationRequestDto> requests = List.of(request1, request2);

            when(workerService.reserveRunnableRequests(10, 3)).thenReturn(requests);

            int result = runner.runOnce(10, 3);

            assertThat(result).isEqualTo(2);

            verify(workerService).reserveRunnableRequests(10, 3);
            verify(sendService).sendOne(request1);
            verify(sendService).sendOne(request2);
        }
    }

    @Nested
    @DisplayName("입력값 검증")
    class Validation {

        @Test
        @DisplayName("limit이 0 이하이면 예외 발생")
        void runOnce_invalidLimit() {
            assertThatThrownBy(() -> runner.runOnce(0, 3))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("limit는 1 이상이어야 합니다.");

            verifyNoInteractions(workerService);
            verifyNoInteractions(sendService);
        }

        @Test
        @DisplayName("maxRetryCount가 음수이면 예외 발생")
        void runOnce_invalidMaxRetryCount() {
            assertThatThrownBy(() -> runner.runOnce(10, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxRetryCount는 0 이상이어야 합니다.");

            verifyNoInteractions(workerService);
            verifyNoInteractions(sendService);
        }
    }

    private NotificationRequestDto createRequest(Long requestId) {
        NotificationRequestDto request = new NotificationRequestDto();
        request.setId(requestId);

        return request;
    }
}