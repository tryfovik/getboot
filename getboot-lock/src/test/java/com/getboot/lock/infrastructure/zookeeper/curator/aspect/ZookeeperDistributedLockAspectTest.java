/*
 * Copyright (c) 2026 qiheng. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getboot.lock.infrastructure.zookeeper.curator.aspect;

import com.getboot.lock.api.annotation.DistributedLock;
import com.getboot.lock.api.constant.DistributedLockConstants;
import com.getboot.lock.api.exception.DistributedLockException;
import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.support.DefaultDistributedLockAcquireFailureHandler;
import com.getboot.lock.support.SpelDistributedLockKeyResolver;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZookeeperDistributedLockAspectTest {

    @Test
    void shouldRejectSecondConcurrentInvocationForSameKey() throws Exception {
        try (TestingServer server = new TestingServer()) {
            CuratorFramework curatorFramework = createCuratorFramework(server);
            try {
                LockProperties properties = createZookeeperLockProperties();
                ZookeeperDistributedLockAspect aspect = new ZookeeperDistributedLockAspect(
                        curatorFramework,
                        new SpelDistributedLockKeyResolver(),
                        new DefaultDistributedLockAcquireFailureHandler(),
                        properties
                );

                CountDownLatch entered = new CountDownLatch(1);
                CountDownLatch release = new CountDownLatch(1);
                BlockingOrderService target = new BlockingOrderService(entered, release);

                AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
                proxyFactory.addAspect(aspect);
                BlockingOrderService proxy = proxyFactory.getProxy();

                ExecutorService executor = Executors.newFixedThreadPool(2);
                try {
                    Future<String> first = executor.submit(() -> proxy.process("order-1"));
                    assertTrue(entered.await(2, TimeUnit.SECONDS));

                    Future<String> second = executor.submit(() -> proxy.process("order-1"));
                    ExecutionException executionException = assertThrows(ExecutionException.class, second::get);
                    assertTrue(executionException.getCause() instanceof DistributedLockException);

                    release.countDown();
                    assertEquals("order-1", first.get(2, TimeUnit.SECONDS));
                    assertEquals(1, target.executions.get());
                } finally {
                    release.countDown();
                    executor.shutdownNow();
                }
            } finally {
                curatorFramework.close();
            }
        }
    }

    @Test
    void shouldNotProceedWhenFailureHandlerReturnsNormally() throws Exception {
        try (TestingServer server = new TestingServer()) {
            CuratorFramework lockHolder = createCuratorFramework(server);
            CuratorFramework contender = createCuratorFramework(server);
            try {
                InterProcessMutex heldLock = new InterProcessMutex(
                        lockHolder,
                        "/getboot/lock/ZGlzdHJpYnV0ZWRfbG9jazpvcmRlciNvcmRlci0y"
                );
                heldLock.acquire();

                LockProperties properties = createZookeeperLockProperties();
                DistributedLockAcquireFailureHandler noOpHandler = (lockKey, distributedLock) -> {
                };
                ZookeeperDistributedLockAspect aspect = new ZookeeperDistributedLockAspect(
                        contender,
                        new SpelDistributedLockKeyResolver(),
                        noOpHandler,
                        properties
                );

                SimpleOrderService target = new SimpleOrderService();
                AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
                proxyFactory.addAspect(aspect);
                SimpleOrderService proxy = proxyFactory.getProxy();

                assertThrows(DistributedLockException.class, () -> proxy.process("order-2"));
                assertEquals(0, target.executions.get());
                heldLock.release();
            } finally {
                contender.close();
                lockHolder.close();
            }
        }
    }

    @Test
    void shouldRejectExplicitExpireTimeForZookeeperLock() throws Exception {
        try (TestingServer server = new TestingServer()) {
            CuratorFramework curatorFramework = createCuratorFramework(server);
            try {
                LockProperties properties = createZookeeperLockProperties();
                ZookeeperDistributedLockAspect aspect = new ZookeeperDistributedLockAspect(
                        curatorFramework,
                        new SpelDistributedLockKeyResolver(),
                        new DefaultDistributedLockAcquireFailureHandler(),
                        properties
                );

                ExpiringOrderService target = new ExpiringOrderService();
                AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
                proxyFactory.addAspect(aspect);
                ExpiringOrderService proxy = proxyFactory.getProxy();

                DistributedLockException exception =
                        assertThrows(DistributedLockException.class, () -> proxy.process("order-3"));
                assertTrue(exception.getMessage().contains("does not support explicit expireTime"));
                assertEquals(0, target.executions.get());
            } finally {
                curatorFramework.close();
            }
        }
    }

    private CuratorFramework createCuratorFramework(TestingServer server) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
                server.getConnectString(),
                new RetryOneTime(100)
        );
        curatorFramework.start();
        assertTrue(curatorFramework.blockUntilConnected(3, TimeUnit.SECONDS));
        return curatorFramework;
    }

    private LockProperties createZookeeperLockProperties() {
        LockProperties properties = new LockProperties();
        properties.setType(DistributedLockConstants.LOCK_TYPE_ZOOKEEPER);
        properties.getZookeeper().setEnabled(true);
        properties.getZookeeper().setBasePath("/getboot/lock");
        return properties;
    }

    static class BlockingOrderService {

        private final CountDownLatch entered;
        private final CountDownLatch release;
        private final AtomicInteger executions = new AtomicInteger();

        BlockingOrderService(CountDownLatch entered, CountDownLatch release) {
            this.entered = entered;
            this.release = release;
        }

        @DistributedLock(scene = "order", keyExpression = "#orderNo", waitTime = 0)
        public String process(String orderNo) {
            executions.incrementAndGet();
            entered.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            return orderNo;
        }
    }

    static class SimpleOrderService {

        private final AtomicInteger executions = new AtomicInteger();

        @DistributedLock(scene = "order", keyExpression = "#orderNo", waitTime = 0)
        public String process(String orderNo) {
            executions.incrementAndGet();
            return orderNo;
        }
    }

    static class ExpiringOrderService {

        private final AtomicInteger executions = new AtomicInteger();

        @DistributedLock(scene = "order", keyExpression = "#orderNo", waitTime = 0, expireTime = 500)
        public String process(String orderNo) {
            executions.incrementAndGet();
            return orderNo;
        }
    }
}
