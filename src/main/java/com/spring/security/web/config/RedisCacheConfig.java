package com.spring.security.web.config;

import com.spring.security.web.constant.RedisCache;
import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJackson3Codec;
import org.redisson.config.Config;
import org.redisson.misc.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
public class RedisCacheConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisConnectionFactory redisConnectionFactory) {
        final String REDIS_ADDRESS = RedisURI.REDIS_PROTOCOL + "%s:%d";
        RedisStandaloneConfiguration redisConfig =
                ((LettuceConnectionFactory) redisConnectionFactory).getStandaloneConfiguration();
        Config redissonConfig = new Config();
        redissonConfig.setCodec(new JsonJackson3Codec());
        redissonConfig.setUsername(redisConfig.getUsername());
        redissonConfig.setPassword(new String(redisConfig.getPassword().get()));
        redissonConfig
                .useSingleServer()
                .setAddress(REDIS_ADDRESS.formatted(redisConfig.getHostName(), redisConfig.getPort()))
                .setDatabase(redisConfig.getDatabase());
        return Redisson.create(redissonConfig);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 启用锁机制
        RedisCacheWriter cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory);
        // 序列化配置
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer.builder()
                                .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                                        .allowIfBaseType(Object.class)
                                        .build())
                                .enableSpringCacheNullValueSupport()
                                .build()))
                .entryTtl(Duration.ofMillis(RedisCache.DEFAULT_TTL))
                .disableCachingNullValues();
        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultCacheConfiguration)
                .transactionAware()
                .build();
    }
}
