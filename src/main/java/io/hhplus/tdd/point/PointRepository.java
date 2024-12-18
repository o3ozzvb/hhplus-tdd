package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class PointRepository {

    private final UserPointTable userPointTable;

    public Optional<UserPoint> selectById(long id) {
        return Optional.ofNullable(userPointTable.selectById(id));
    }

    public UserPoint insertOrUpdate(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
    }
}
