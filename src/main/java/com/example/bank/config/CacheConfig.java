package com.example.bank.config;

import com.example.bank.model.Account;
import com.example.bank.persistence.AccountReader;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * Sets up Redis caching for account lookups.
 * Can be switched off with app.cache.enabled=false.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig implements CachingConfigurer {

    /**
     * Stores accounts in Redis, expiring each entry after the configured time (10 minutes by default).
     */
    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                   @Value("${app.cache.ttl-minutes:10}") long ttlMinutes) {
        RedisCacheConfiguration accounts = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(Account.class)));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(AccountReader.CACHE_NAME, accounts)
                .build();
    }

    /** If Redis is down, log and fall through to Postgres instead of failing the request. */
    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }
}
