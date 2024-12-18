package io.hhplus.tdd.point;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public static PointHistory createChargeHistory(long id, long amount, long updateMillis) {
        return new PointHistory(0L, id, amount, TransactionType.CHARGE, updateMillis);
    }

    public static PointHistory createUseHistory(long id, long amount, long updateMillis) {
        return new PointHistory(0L, id, amount, TransactionType.USE, updateMillis);
    }
}
