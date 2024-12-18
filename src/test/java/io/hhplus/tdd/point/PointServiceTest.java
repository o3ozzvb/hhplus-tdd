package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.exception.PointErrorCode;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    /**
     * 존재하지 않는 사용자
     */
    @Test
    void 포인트_조회_실패() {
        // given
        long id = -1L;

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
        UserPoint expectedUserPoint = new UserPoint(id, 1000L, System.currentTimeMillis());

        // when
        when(pointRepository.selectById(id))
                .thenReturn(Optional.of(expectedUserPoint));

        // then
        UserPoint userPoint = pointService.getUserPoint(id);
        assertThat(userPoint.id()).isEqualTo(expectedUserPoint.id());
        assertThat(userPoint.point()).isEqualTo(expectedUserPoint.point());
        assertThat(userPoint.updateMillis()).isEqualTo(expectedUserPoint.updateMillis());
    }

    /**
     * 내역이 없을 경우 빈 리스트 반환
     */
    @Test
    void 포인트_내역_조회_빈리스트() {
        // given
        long id = -1L;

        when(pointHistoryRepository.selectAllByuserId(id))
                .thenReturn(Collections.emptyList());

        // when
        List<PointHistory> pointHistoryList = pointService.getUserPointHistory(id);

        // then
        assertThat(pointHistoryList).isEmpty();
    }

    @Test
    void 포인트_내역_조회_성공() {
        // given
        long id = 0L;

        List<PointHistory> expectedPointHistoryList = List.of(
                new PointHistory(0L, id, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(0L, id, 700L, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryRepository.selectAllByuserId(id))
                .thenReturn(expectedPointHistoryList);

        // when
        List<PointHistory> pointHistoryList = pointService.getUserPointHistory(id);

        // then
        assertThat(pointHistoryList).hasSize(2)
                .extracting(PointHistory::id, PointHistory::userId, PointHistory::amount, PointHistory::type, PointHistory::updateMillis)
                .containsExactlyInAnyOrder(
                        tuple(
                                expectedPointHistoryList.get(0).id(),
                                expectedPointHistoryList.get(0).userId(),
                                expectedPointHistoryList.get(0).amount(),
                                expectedPointHistoryList.get(0).type(),
                                expectedPointHistoryList.get(0).updateMillis()
                        ),
                        tuple(
                                expectedPointHistoryList.get(1).id(),
                                expectedPointHistoryList.get(1).userId(),
                                expectedPointHistoryList.get(1).amount(),
                                expectedPointHistoryList.get(1).type(),
                                expectedPointHistoryList.get(1).updateMillis()
                        )
                );
    }

    /**
     * 충전금액이 0보다 작으면 예외 발생
     */
    @Test
    void 포인트_충전_실패() {
        // given
        long id = 0L;
        long amount = -100L;

        // when

        // then
        assertThatThrownBy(() -> pointService.charge(id, amount))
                .isInstanceOf(PointException.class)
                .hasMessage(PointErrorCode.CHARGE_AMOUNT_LESS_THAN_ZERO.getMessage());
    }

    @Test
    void 포인트_충전_성공() {
        // given
        long id = 0L;
        long amount = 100L;

        when(pointRepository.selectById(id))
                .thenReturn(Optional.empty());

        UserPoint expectedUserPoint = new UserPoint(id, amount, System.currentTimeMillis());

        when(pointRepository.insertOrUpdate(any(UserPoint.class)))
                .thenReturn(expectedUserPoint);

        // when
        UserPoint userPoint = pointService.charge(id, amount);

        // then
        assertThat(userPoint.id()).isEqualTo(expectedUserPoint.id());
        assertThat(userPoint.point()).isEqualTo(expectedUserPoint.point());
        assertThat(userPoint.updateMillis()).isEqualTo(expectedUserPoint.updateMillis());
    }
}