# getboot-cache

缓存基础 starter，统一承接 Redis 接入、序列化模板和常见缓存操作门面。

## 作用

- 提供统一的 Redis 接入能力
- 提供默认 JSON 序列化的 `RedisTemplate`
- 提供开箱即用的 `CacheOperator`

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-cache</artifactId>
</dependency>
```

适合这几类场景：

- 想统一 Redis 配置和默认模板
- 想用稳定的 `CacheOperator`，而不是让业务代码散落 `RedisTemplate` 操作
- 准备继续接入依赖 Redis 的能力，例如锁、Webhook、鉴权等

## 前置条件

- 业务环境需要有可访问的 Redis
- 至少准备 `getboot.cache.redis.*` 对应的连接配置
- 如果还需要分布式锁、限流等 Redisson 能力，要额外引入 `getboot-coordination`

## 目录约定

- `api.operator`：对外稳定的缓存操作门面
- `spi.redis`：Redis 序列化和模板定制扩展点
- `infrastructure.redis.autoconfigure`：自动装配入口
- `infrastructure.redis.environment`：配置别名与环境适配
- `infrastructure.redis.support`：默认 Redis 实现

## 配置示例

```yaml
getboot:
  cache:
    redis:
      host: 127.0.0.1        # Redis 主机地址
      port: 6379             # Redis 端口
      password: ""           # Redis 密码，没有可留空
      database: 0            # Redis 数据库索引
```

## 默认 Bean

- `RedisSerializer<Object>`：Bean 名称为 `getbootRedisValueSerializer`
- `RedisTemplate<String, Object>`：Bean 名称为 `getbootRedisTemplate`，同时标记为 `@Primary`
- `CacheOperator`：默认实现为 `RedisCacheOperator`

## 扩展点

- `RedisObjectMapperCustomizer`：继续定制 Redis JSON 序列化使用的 `ObjectMapper`
- `GetbootRedisTemplateCustomizer`：继续定制 `RedisTemplate<String, Object>`
- 业务项目统一使用 `getboot.cache.redis.*`，不直接暴露底层组件原生前缀
- Redis 实现相关代码统一收敛在 `com.getboot.cache.infrastructure.redis.*`
- 若业务还需要分布式锁、限流等 Redisson 能力，应额外引入 `getboot-coordination`

## 已实现技术栈

- Spring Data Redis
- Jackson JSON Serializer

## 使用示例

```java
import com.getboot.cache.api.operator.CacheOperator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DemoCacheService {

    private final CacheOperator cacheOperator;
    private final RedisTemplate<String, Object> redisTemplate;

    public DemoCacheService(CacheOperator cacheOperator, RedisTemplate<String, Object> redisTemplate) {
        this.cacheOperator = cacheOperator;
        this.redisTemplate = redisTemplate;
    }

    public void saveUser(UserCache userCache) {
        cacheOperator.set("user:" + userCache.getUserId(), userCache);
        redisTemplate.opsForValue().set("user:last", userCache);
    }
}
```

补充说明：

- 默认模板的 Key / HashKey 使用字符串序列化，Value / HashValue 使用 JSON 序列化
- 默认序列化不会启用全局宽松多态反序列化；若业务需要更激进的跨类型反序列化，请自行提供定制模板或 `RedisObjectMapperCustomizer`
- 可直接参考 `src/main/resources/getboot-cache-redis.yml.example`
