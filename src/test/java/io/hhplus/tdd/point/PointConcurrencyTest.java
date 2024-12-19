package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 포인트 동시성 제어 테스트
 */
@SpringBootTest
public class PointConcurrencyTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    @Test
    void 충전_동시성_제어_테스트() throws InterruptedException {
        // given
        long id = 1L;
        long amount = 100L;

        pointRepository.insertOrUpdate(new UserPoint(id, 0L, System.currentTimeMillis()));

        int threadCount = 30;

        // when
        this.executorService(threadCount, () -> pointService.charge(id, amount));

        // then
        Optional<UserPoint> optionalUserPoint = pointRepository.selectById(id);
        assertThat(optionalUserPoint).isPresent();
        UserPoint userPoint = optionalUserPoint.get();
        assertThat(userPoint.point()).isEqualTo(amount * threadCount);
    }

    /**
     * 여러 명이 동시에 충전할 경우
     */
    @Test
    void 충전_동시성_제어_테스트2() throws InterruptedException {
        // given
        long[] ids = {1, 2, 3, 4, 5};
        long amount = 100L;

        for (long id : ids) {
            pointRepository.insertOrUpdate(new UserPoint(id, 0L, System.currentTimeMillis()));
        }

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            int id = i % ids.length + 1; // id 당 4번 반복
            executorService.submit(() -> {
                try {
                    pointService.charge(id, amount);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        for (long id : ids) {
            Optional<UserPoint> optionalUserPoint = pointRepository.selectById(id);
            assertThat(optionalUserPoint).isPresent();

            UserPoint userPoint = optionalUserPoint.get();
            assertThat(userPoint.point()).isEqualTo(amount * 4);
        }
    }

    @Test
    void 사용_동시성_제어_테스트() throws InterruptedException {
        // given
        long id = 1L;
        long balance = 10000L;
        long amount = 100L;

        pointRepository.insertOrUpdate(new UserPoint(id, balance, System.currentTimeMillis()));

        int threadCount = 30;

        // when
        this.executorService(threadCount, () -> pointService.use(id, amount));

        // then
        Optional<UserPoint> optionalUserPoint = pointRepository.selectById(id);
        assertThat(optionalUserPoint).isPresent();
        UserPoint userPoint = optionalUserPoint.get();
        assertThat(userPoint.point()).isEqualTo(balance - amount * threadCount);
    }

    /**
     * 여러 명이 동시에 사용할 경우
     */
    @Test
    void 사용_동시성_제어_테스트2() throws InterruptedException {
        // given
        long[] ids = {1, 2, 3, 4, 5};
        long balance = 10000L;
        long amount = 100L;

        for (long id : ids) {
            pointRepository.insertOrUpdate(new UserPoint(id, balance, System.currentTimeMillis()));
        }

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            int id = i % ids.length + 1; // id 당 4번 반복
            executorService.submit(() -> {
                try {
                    pointService.use(id, amount);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        for (long id : ids) {
            Optional<UserPoint> optionalUserPoint = pointRepository.selectById(id);
            assertThat(optionalUserPoint).isPresent();

            UserPoint userPoint = optionalUserPoint.get();
            assertThat(userPoint.point()).isEqualTo(balance - amount * 4);
        }
    }

    /**
     * 하나의 id로 충전과 사용을 동시에 시도할 경우
     */
    @Test
    void 충전_사용_동시성_제어_테스트() throws InterruptedException {
        // given
        long id = 0L;
        long balance = 1000L;
        long chargeAmount = 100L;
        long useAmount = 30L;

        pointRepository.insertOrUpdate(new UserPoint(id, balance, System.currentTimeMillis()));

        // when
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    if (idx % 2 == 0) {  // 충전 사용 번갈아가면서 시도
                        pointService.charge(id, chargeAmount);
                    } else {
                        pointService.use(id, useAmount);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Optional<UserPoint> optionalUserPoint = pointRepository.selectById(id);
        assertThat(optionalUserPoint).isPresent();

        UserPoint userPoint = optionalUserPoint.get();
        assertThat(userPoint.point()).isEqualTo(balance + chargeAmount * (threadCount/2) - useAmount * (threadCount/2));
    }

    private void executorService(int threadCount, Runnable task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }

}
