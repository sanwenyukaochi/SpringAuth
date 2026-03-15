package com.spring.security.web.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.misc.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisCacheConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisConnectionFactory redisConnectionFactory) {
        final String REDIS_ADDRESS = RedisURI.REDIS_PROTOCOL + "%s:%d";
        RedisStandaloneConfiguration redisConfig =
                ((LettuceConnectionFactory) redisConnectionFactory).getStandaloneConfiguration();
        Config redissonConfig = new Config();
        redissonConfig.setCodec(new JsonJacksonCodec());
        redissonConfig.setUsername(redisConfig.getUsername());
        redissonConfig.setPassword(new String(redisConfig.getPassword().get()));
        redissonConfig
                .useSingleServer()
                .setAddress(REDIS_ADDRESS.formatted(redisConfig.getHostName(), redisConfig.getPort()))
                .setDatabase(redisConfig.getDatabase());
        return Redisson.create(redissonConfig);
    }
}
