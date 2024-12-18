package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.exception.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    /**
     * 포인트 조회
     * @param id 사용자 id
     */
    public UserPoint getUserPoint(long id) {
        return pointRepository.selectById(id)
                .orElseThrow(() -> new PointException(PointErrorCode.USER_ID_NOT_EXIST));
    }
}
