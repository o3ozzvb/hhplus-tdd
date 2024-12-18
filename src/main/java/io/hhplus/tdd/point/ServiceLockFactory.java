package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ServiceLockFactory {
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(long id) {
        return lockMap.computeIfAbsent(id, key -> new ReentrantLock());
    }
}
