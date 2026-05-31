package portfolio.notification_lab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.dto.NotificationRequestDto;
import portfolio.notification_lab.mapper.NotificationWorkerMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerServiceTest {

    @Mock
    private NotificationWorkerMapper mapper;

    @InjectMocks
    private NotificationWorkerServiceImpl service;

    @Nested
    @DisplayName("처리 가능한 요청 RESERVED 선점")
    class ReserveRunnableRequests {

        @Test
        @DisplayName("PENDING과 재시도 가능한 FAILED 요청을 RESERVED로 선점")
        void reserveRunnableRequests_success() {
            int limit = 10;
            int maxRetryCount = 3;

            List<NotificationRequestDto> reservedRequests = List.of(
                    new NotificationRequestDto(),
                    new NotificationRequestDto()
            );

            when(mapper.reserveRunnableRequests(limit, maxRetryCount))
                    .thenReturn(reservedRequests);

            List<NotificationRequestDto> result = service.reserveRunnableRequests(limit, maxRetryCount);

            assertThat(result).isSameAs(reservedRequests);

            verify(mapper).reserveRunnableRequests(limit, maxRetryCount);
        }

        @Test
        @DisplayName("limit이 0이하 이면 예외 발생")
        void reserveRunnableRequests_invalidLimit() {
            int limit = 0;
            int maxRetryCount = 3;

            assertThatThrownBy(() -> service.reserveRunnableRequests(limit, maxRetryCount))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("maxRetryCount가 음수이면 예외 발생")
        void reserveRunnableRequests_invalidMaxRetryCount() {
            int limit = 10;
            int maxRetryCount = -1;

            assertThatThrownBy(() -> service.reserveRunnableRequests(limit, maxRetryCount))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(mapper);
        }
    }
}