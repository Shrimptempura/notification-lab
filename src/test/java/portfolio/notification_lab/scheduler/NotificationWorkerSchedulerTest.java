package portfolio.notification_lab.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.config.NotificationRetryProperties;
import portfolio.notification_lab.config.NotificationWorkerProperties;
import portfolio.notification_lab.worker.NotificationWorkerRunner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerSchedulerTest {

    @Mock
    private NotificationWorkerRunner workerRunner;

    @Mock
    private NotificationWorkerProperties workerProperties;

    @Mock
    private NotificationRetryProperties retryProperties;

    @InjectMocks
    private NotificationWorkerScheduler scheduler;

    @Test
    @DisplayName("worker가 활성화 되어 있으면 설정값으로 worker를 실행한다")
    void runWorker_enabled() {
        when(workerProperties.enabled()).thenReturn(true);
        when(workerProperties.limit()).thenReturn(1000);
        when(retryProperties.maxRetryCount()).thenReturn(3);

        when(workerRunner.runOnce(1000, 3)).thenReturn(100);

        scheduler.runWorker();

        verify(workerProperties).enabled();
        verify(workerProperties).limit();   // = verity(workerProperties, times(1)).limit();
        verify(retryProperties).maxRetryCount();
        verify(workerRunner).runOnce(1000, 3);
    }

    @Test
    @DisplayName("worker가 비활성화 되어있으면 실행하지 않는다")
    void runWorker_disabled() {
        when(workerProperties.enabled()).thenReturn(false);

        scheduler.runWorker();

        verify(workerProperties).enabled();
        verify(workerProperties, never()).limit();
        verifyNoInteractions(retryProperties);
        verifyNoInteractions(workerRunner);
    }

    @Test
    @DisplayName("처리할 요청이 없어도 worker 실행은 수행한다")
    void runWorker_enabled_emptyProcessCount() {
        when(workerProperties.enabled()).thenReturn(true);
        when(workerProperties.limit()).thenReturn(1000);
        when(retryProperties.maxRetryCount()).thenReturn(3);

        when(workerRunner.runOnce(1000, 3)).thenReturn(0);

        scheduler.runWorker();

        verify(workerRunner).runOnce(1000, 3);
    }

}