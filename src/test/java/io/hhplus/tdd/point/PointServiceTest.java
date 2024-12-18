package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.exception.PointErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    /**
     * 존재하지 않는 사용자
     */
    @Test
    void 포인트_조회_실패() {
        // given
        long id = -1;

        // when
        when(pointRepository.selectById(id))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> pointService.getUserPoint(id))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.USER_ID_NOT_EXIST.getMessage());
    }

    @Test
    void 포인트_조회_성공() {
        // given
        long id = 0L;
        UserPoint userPoint = new UserPoint(id, 1000L, System.currentTimeMillis());

        // when
        when(pointRepository.selectById(id))
                .thenReturn(Optional.of(userPoint));

        // then
        UserPoint selectedUserPoint = pointService.getUserPoint(id);
        assertThat(selectedUserPoint.id()).isEqualTo(userPoint.id());
        assertThat(selectedUserPoint.point()).isEqualTo(userPoint.point());
        assertThat(selectedUserPoint.updateMillis()).isEqualTo(userPoint.updateMillis());
    }
}