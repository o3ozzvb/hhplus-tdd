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
