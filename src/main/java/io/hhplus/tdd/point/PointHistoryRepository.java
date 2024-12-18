package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> selectAllByuserId(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    private PointHistory insert(PointHistory pointHistory) {
        return pointHistoryTable.insert(pointHistory.userId(), pointHistory.amount(), pointHistory.type(), pointHistory.updateMillis());
    }
}
