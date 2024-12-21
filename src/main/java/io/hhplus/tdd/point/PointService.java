package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.exception.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final ServiceLockFactory lockFactory;

    /**
     * 포인트 조회
     * @param id 사용자 id
     * @return 포인트
     */
    public UserPoint getUserPoint(long id) {
        return pointRepository.selectById(id)
                .orElseThrow(() -> new PointException(PointErrorCode.USER_ID_NOT_EXIST));
    }

    /**
     * 포인트 충전/사용 내역 조회
     * @param id 사용자 id
     * @return 포인트 내역
     */
    public List<PointHistory> getUserPointHistory(long id) {
        return pointHistoryRepository.selectAllByuserId(id);
    }

    /**
     * 포인트 충전
     * @param id 사용자 id
     * @param amount 충전포인트
     * @return 충전 후 포인트
     */
    public UserPoint charge(long id, long amount) {
        long MAX_VALUE = 2_000_000_000L;

        // 충전 금액 검증
        if (amount < 0) {
            throw new PointException(PointErrorCode.CHARGE_AMOUNT_LESS_THAN_ZERO);
        }
        if (amount > MAX_VALUE) {
            throw new PointException(PointErrorCode.CHARGE_AMOUNT_GREATER_THAN_MAX);
        }

        ReentrantLock lock = lockFactory.getLock(id);

        lock.lock();

        try {
            // 포인트 충전
            UserPoint userPoint = pointRepository.selectById(id).orElse(UserPoint.empty(id));
            UserPoint chargedPoint = userPoint.charge(amount);
            UserPoint savedUserPoint = pointRepository.insertOrUpdate(chargedPoint);

            // 히스토리 저장
            PointHistory chargeHistory = PointHistory.createChargeHistory(savedUserPoint.id(), savedUserPoint.point(), savedUserPoint.updateMillis());
            pointHistoryRepository.insert(chargeHistory);

            return savedUserPoint;
        } finally {
            lock.unlock();
        }


    }

    /**
     * 포인트 사용
     * @param id 사용자 id
     * @param amount 사용포인트
     */
    public UserPoint use(long id, long amount) {
        // 사용 금액 검증
        // 사용 금액이 0 보다 작으면 예외 발생
        if (amount < 0) {
            throw new PointException(PointErrorCode.USE_AMOUNT_LESS_THAN_ZERO);
        }

        ReentrantLock lock = lockFactory.getLock(id);

        lock.lock();

        try {

            // 사용 금액이 잔액보다 크면 예외 발생
            Optional<UserPoint> userPoint = pointRepository.selectById(id);
            if (userPoint.isEmpty() || userPoint.get().point() < amount) {
                throw new PointException(PointErrorCode.BALANCE_LESS_THAN_USE_AMOUNT);
            }

            // 포인트 사용
            UserPoint usedPoint = userPoint.get().use(amount);
            UserPoint updatedUserPoint = pointRepository.insertOrUpdate(usedPoint);

            // 히스토리 저장
            PointHistory useHistory = PointHistory.createUseHistory(updatedUserPoint.id(), updatedUserPoint.point(), updatedUserPoint.updateMillis());
            pointHistoryRepository.insert(useHistory);

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }
}
