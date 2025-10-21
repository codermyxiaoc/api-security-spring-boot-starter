package cn.coderxiaoc.signature;

import cn.coderxiaoc.property.SignatureProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InMemoryPreventDuplicate extends PreventDuplicateAbstract {
    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private final SignatureProperty signatureProperty;
    private final ScheduledExecutorService scheduler;

    public InMemoryPreventDuplicate(SignatureProperty properties) {
        this.signatureProperty = properties;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "prevent-duplicate-cleaner");
            thread.setDaemon(true);
            return thread;
        });

        long cleanInterval = this.signatureProperty.getInMemoryCleanInterval();
        this.scheduler.scheduleAtFixedRate(
                this::cleanExpiredKeys,
                cleanInterval,
                cleanInterval,
                signatureProperty.getInMemoryCleanIntervalTimeUnit()
        );
    }

    public boolean isDuplicate(String key, MethodParameter parameter) {
        if (!StringUtils.hasText(key)) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + TimeUnit.MILLISECONDS.convert(
                 getPreventDuplicateTimeout( parameter),
                getPreventDuplicateTimeUnit( parameter)
        );

        Long existingExpireTime = cache.putIfAbsent(key, expireTime);

        if (existingExpireTime == null) {
            return true;
        }

        if (existingExpireTime > currentTime) {
            return false;
        }
        return true;
    }

    public void removeKey(String key) {
        if (StringUtils.hasText(key)) {
            cache.remove(key);
        }
    }

    private void cleanExpiredKeys() {
        try {
            long currentTime = System.currentTimeMillis();
            cache.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        } catch (Exception e) {
            System.err.println("Clean expired keys error: " + e.getMessage());
        }
    }

    public int getCacheSize() {
        return cache.size();
    }
    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        cache.clear();
    }

    @Override
    public boolean preventDuplicate(HttpInputMessage inputMessage, MethodParameter parameter) {
        String preventDuplicateField = getPreventDuplicateField(parameter);
        String preventDuplicateValue = getPreventDuplicateValue(preventDuplicateField, inputMessage);
        if (!StringUtils.hasText(preventDuplicateValue)) {
            return false;
        }
        return isDuplicate(preventDuplicateValue, parameter);
    }

    @Override
    SignatureProperty getSignatureProperty() {
        return signatureProperty;
    }
}
