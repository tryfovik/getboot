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
package com.getboot.limiter.support.registry;

import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.api.registry.RateLimiterRegistry;
import com.getboot.limiter.spi.RateLimiterAlgorithmHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 默认限流器注册表。
 *
 * <p>统一管理规则缓存、预定义规则装载与算法路由，外部只需面对一个稳定注册表接口。</p>
 *
 * @author qiheng
 */
@Slf4j
public class DefaultRateLimiterRegistry implements RateLimiterRegistry {

    private static final long RETRY_INTERVAL_MILLIS = 50L;

    private final Map<LimiterAlgorithm, RateLimiterAlgorithmHandler> algorithmHandlers;
    private final RateLimiterAlgorithmHandler defaultHandler;
    private final Map<String, LimiterRule> configuredRules = new ConcurrentHashMap<>();

    public DefaultRateLimiterRegistry(Collection<RateLimiterAlgorithmHandler> algorithmHandlers) {
        if (algorithmHandlers == null || algorithmHandlers.isEmpty()) {
            throw new IllegalArgumentException("At least one rate limiter algorithm handler is required.");
        }
        EnumMap<LimiterAlgorithm, RateLimiterAlgorithmHandler> handlerMap =
                new EnumMap<>(LimiterAlgorithm.class);
        for (RateLimiterAlgorithmHandler handler : algorithmHandlers) {
            RateLimiterAlgorithmHandler previous = handlerMap.putIfAbsent(handler.algorithm(), handler);
            if (previous != null) {
                throw new IllegalStateException("Duplicate rate limiter algorithm handler: " + handler.algorithm());
            }
        }
        this.algorithmHandlers = handlerMap;
        this.defaultHandler = handlerMap.getOrDefault(
                LimiterAlgorithm.SLIDING_WINDOW,
                handlerMap.values().iterator().next()
        );
        validatePredefinedRules();
    }

    @Override
    public void configureRateLimiter(String limiterName, LimiterRule config) {
        validateLimiterName(limiterName);
        if (config == null) {
            throw new IllegalArgumentException("Limiter configuration must not be null.");
        }
        LimiterRule normalizedRule = normalizeRule(config, defaultHandler.algorithm());
        getHandler(normalizedRule.getAlgorithm()).validateRule(normalizedRule);
        configuredRules.put(limiterName, normalizedRule);
        log.info("Configured limiter. limiter={}, algorithm={}, rule={}",
                limiterName, normalizedRule.getAlgorithm(), normalizedRule);
    }

    @Override
    public boolean tryAcquire(String limiterName) {
        ResolvedLimiter resolvedLimiter = resolveLimiter(limiterName);
        return tryAcquire(resolvedLimiter, 1, resolvedLimiter.handler.defaultTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public boolean tryAcquire(String limiterName, long permits) {
        ResolvedLimiter resolvedLimiter = resolveLimiter(limiterName);
        return tryAcquire(resolvedLimiter, permits, resolvedLimiter.handler.defaultTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public boolean tryAcquire(String limiterName, long timeout, TimeUnit timeUnit) {
        return tryAcquire(limiterName, 1, timeout, timeUnit);
    }

    @Override
    public boolean tryAcquire(String limiterName, long permits, long timeout, TimeUnit timeUnit) {
        ResolvedLimiter resolvedLimiter = resolveLimiter(limiterName);
        return tryAcquire(resolvedLimiter, permits, timeout, timeUnit);
    }

    @Override
    public void updateRateLimiterConfig(String limiterName, LimiterRule newConfig) {
        configureRateLimiter(limiterName, newConfig);
    }

    @Override
    public boolean deleteRateLimiter(String limiterName) {
        validateLimiterName(limiterName);
        configuredRules.remove(limiterName);
        boolean deleted = false;
        for (RateLimiterAlgorithmHandler handler : algorithmHandlers.values()) {
            deleted |= handler.delete(limiterName);
        }
        return deleted;
    }

    private boolean tryAcquire(ResolvedLimiter resolvedLimiter, long permits, long timeout, TimeUnit timeUnit) {
        if (permits <= 0) {
            throw new IllegalArgumentException("Permits must be greater than 0.");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        }
        long timeoutMillis = Math.max(0L, timeUnit.toMillis(timeout));
        long deadline = System.currentTimeMillis() + timeoutMillis;
        do {
            if (resolvedLimiter.handler.tryAcquire(resolvedLimiter.limiterName, resolvedLimiter.rule, permits)) {
                return true;
            }
            if (timeoutMillis <= 0L) {
                return false;
            }
            long remainingMillis = deadline - System.currentTimeMillis();
            if (remainingMillis <= 0L) {
                return false;
            }
            sleep(Math.min(RETRY_INTERVAL_MILLIS, remainingMillis));
        } while (true);
    }

    private ResolvedLimiter resolveLimiter(String limiterName) {
        validateLimiterName(limiterName);
        LimiterRule rule = configuredRules.computeIfAbsent(limiterName, this::resolveRuleFromDefinitions);
        return new ResolvedLimiter(limiterName, rule, getHandler(rule.getAlgorithm()));
    }

    private LimiterRule resolveRuleFromDefinitions(String limiterName) {
        LimiterRule matchedRule = null;
        LimiterAlgorithm matchedAlgorithm = null;
        for (RateLimiterAlgorithmHandler handler : algorithmHandlers.values()) {
            LimiterRule candidate = handler.predefinedRules().get(limiterName);
            if (candidate == null) {
                continue;
            }
            if (matchedRule != null) {
                throw new IllegalStateException("Limiter name '" + limiterName
                        + "' is defined by multiple algorithms: " + matchedAlgorithm + ", " + handler.algorithm());
            }
            matchedRule = normalizeRule(candidate, handler.algorithm());
            matchedAlgorithm = handler.algorithm();
            handler.validateRule(matchedRule);
        }
        if (matchedRule != null) {
            return matchedRule;
        }
        LimiterRule defaultRule = normalizeRule(defaultHandler.defaultRule(), defaultHandler.algorithm());
        defaultHandler.validateRule(defaultRule);
        return defaultRule;
    }

    private LimiterRule normalizeRule(LimiterRule rule, LimiterAlgorithm fallbackAlgorithm) {
        LimiterRule normalizedRule = rule.copy();
        normalizedRule.setAlgorithm(rule.resolveAlgorithm(fallbackAlgorithm));
        return normalizedRule;
    }

    private RateLimiterAlgorithmHandler getHandler(LimiterAlgorithm algorithm) {
        RateLimiterAlgorithmHandler handler = algorithmHandlers.get(algorithm);
        if (handler == null) {
            throw new IllegalStateException("No rate limiter algorithm handler registered for " + algorithm + ".");
        }
        return handler;
    }

    private void validatePredefinedRules() {
        Map<String, LimiterAlgorithm> limiterOwnership = new HashMap<>();
        for (RateLimiterAlgorithmHandler handler : algorithmHandlers.values()) {
            for (Map.Entry<String, LimiterRule> entry : handler.predefinedRules().entrySet()) {
                LimiterAlgorithm previous = limiterOwnership.putIfAbsent(entry.getKey(), handler.algorithm());
                if (previous != null) {
                    throw new IllegalStateException("Limiter name '" + entry.getKey()
                            + "' is defined by multiple algorithms: " + previous + ", " + handler.algorithm());
                }
                handler.validateRule(normalizeRule(entry.getValue(), handler.algorithm()));
            }
        }
    }

    private void validateLimiterName(String limiterName) {
        if (limiterName == null || limiterName.trim().isEmpty()) {
            throw new IllegalArgumentException("Limiter name must not be blank.");
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for rate limiter permit.", ex);
        }
    }

    private record ResolvedLimiter(
            String limiterName,
            LimiterRule rule,
            RateLimiterAlgorithmHandler handler
    ) {
    }
}
