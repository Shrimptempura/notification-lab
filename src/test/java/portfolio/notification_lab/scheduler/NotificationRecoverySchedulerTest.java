package portfolio.notification_lab.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import portfolio.notification_lab.config.NotificationRecoveryProperties;
import portfolio.notification_lab.service.NotificationRequestStateService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRecoverySchedulerTest {

    @Mock
    private NotificationRequestStateService stateService;

    @Mock
    private NotificationRecoveryProperties recoveryProperties;

    @InjectMocks
    private NotificationRecoveryScheduler scheduler;

    @Test
    @DisplayName("recovery가 활성화 되어 있으면 timeoutMinutes 기준으로 만료된 RESERVED 요청을 복구함")
    void recoverExpiredReservations_enabled() {
        when(recoveryProperties.enabled()).thenReturn(true);
        when(recoveryProperties.timeoutMinutes()).thenReturn(10);
        when(stateService.releaseExpiredReservations(10)).thenReturn(5);

        scheduler.recoverExpiredReservations();

        verify(stateService).releaseExpiredReservations(10);
    }

    @Test
    @DisplayName("recovery가 비활성화 되어 있으면 요청을 복구하지 않는다.")
    void recoverExpiredReservations_disabled() {
        when(recoveryProperties.enabled()).thenReturn(false);

        scheduler.recoverExpiredReservations();

        verify(recoveryProperties, never()).timeoutMinutes();
        verifyNoInteractions(stateService);
    }

    @Test
    @DisplayName("복구 대상이 없어도 정상 종료된다")
    void recoverExpiredReservations_whenReleasedCountIsZero() {
        when(recoveryProperties.enabled()).thenReturn(true);
        when(recoveryProperties.timeoutMinutes()).thenReturn(10);
        when(stateService.releaseExpiredReservations(10)).thenReturn(0);

        scheduler.recoverExpiredReservations();

        verify(stateService).releaseExpiredReservations(10);
    }
}