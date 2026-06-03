package com.taskmanagement.taskmanagement.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;

@Configuration
// Tells Spring: read this class at startup

@EnableCaching
// THE MOST IMPORTANT ANNOTATION!
// Activates Spring's caching mechanism
// Without this → @Cacheable annotations do NOTHING!
// Like @EnableWebSecurity for security — this turns caching ON

public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        // RedisTemplate = main tool for talking to Redis
        // Like JdbcTemplate for databases

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        // Connect to Redis server using connection factory
        // Connection factory reads host/port from application.properties

        template.setKeySerializer(new StringRedisSerializer());
        // Keys are stored as plain Strings in Redis
        // Example key: "tasks:sumit@gmail.com"

        template.setValueSerializer(
                new GenericJackson2JsonRedisSerializer());
        // Values are stored as JSON in Redis
        // Example value: [{"id":1,"title":"Fix bug",...}]
        // JSON format → human readable, easy to debug!

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(
                new GenericJackson2JsonRedisSerializer());
        // Same serializers for hash data structures

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {
        // RedisCacheManager manages all your caches
        // This is what @Cacheable uses internally

        RedisCacheConfiguration cacheConfig =
                RedisCacheConfiguration.defaultCacheConfig()

                .entryTtl(Duration.ofMinutes(10))
                // TTL = Time To Live
                // Each cached item expires after 10 minutes
                // Even if never invalidated!
                // Prevents stale data forever

                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // Store cache keys as Strings

                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(
                            new GenericJackson2JsonRedisSerializer()))
                // Store cache values as JSON

                .disableCachingNullValues();
                // Never cache null values

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
