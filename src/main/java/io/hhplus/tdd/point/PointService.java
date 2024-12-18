package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.exception.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

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
}
