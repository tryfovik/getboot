# getboot-database

数据访问增强 starter，提供数据源预热、MongoDB 启动校验、MyBatis-Plus 拦截器、审计字段填充与 ShardingSphere 分库分表接入能力。

## 作用

- 提供数据源启动预热与连通性校验
- 提供 MongoDB 启动预热、连通性校验与配置桥接
- 提供 MyBatis-Plus 分页、乐观锁、防全表更新拦截器
- 提供默认审计字段自动填充处理器
- 提供 ShardingSphere 分库分表接入与规则文件占位符解析

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-database</artifactId>
</dependency>
```

适合这几类场景：

- 想统一数据源初始化、连通性校验和启动失败策略
- 想统一 MongoTemplate 启动探活、启动后连通性校验和自动索引开关
- 想开箱即用 MyBatis-Plus 分页、乐观锁和审计字段填充
- 想把 ShardingSphere 的规则文件接入和配置前缀收敛在模块内

## 前置条件

- 需要准备可访问的数据库与基础连接信息
- 如果使用分库分表，需要额外准备 ShardingSphere 规则 YAML 文件
- 如果还要接 Seata，建议再引入 `getboot-transaction`，并明确事务组合策略
- 当前模块会优先从 `getboot.database.*` 承接配置，再桥接到底层组件原生前缀

## 目录约定

- `api.properties`：对外稳定配置模型
- `support.datasource`：内部数据源初始化辅助能力
- `support.mongodb`：内部 MongoDB 初始化辅助能力
- `infrastructure.datasource.*`：数据源预热与初始化
- `infrastructure.mongodb.*`：MongoDB 初始化与自动配置
- `infrastructure.mybatisplus.*`：MyBatis-Plus 增强
- `infrastructure.sharding.*`：ShardingSphere 接入

## 配置示例

```yaml
getboot:
  database:
    enabled: true                 # 是否启用数据库增强能力
    datasource:
      url: jdbc:mysql://127.0.0.1:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai
      username: root              # 数据库用户名
      password: 123456            # 数据库密码
      driver-class-name: com.mysql.cj.jdbc.Driver
      init:
        enabled: true             # 是否在启动阶段主动初始化数据源
        strict-mode: true         # 初始化失败时是否中断启动
        timeout: 30000            # 初始化超时时间，单位毫秒
        validate-after-startup: true # 应用启动完成后是否再次校验连接
    mongodb:
      enabled: true
      uri: mongodb://127.0.0.1:27017/demo
      auto-index-creation: false
      init:
        enabled: true
        strict-mode: true
        validate-after-startup: true
    mybatis-plus:
      configuration:
        map-underscore-to-camel-case: true # MyBatis-Plus 额外配置示例
    sharding:
      enabled: false
      rule-config: classpath:sharding/demo-sharding-rule.yaml
      reuse-bean-datasources: true
      data-source-beans:
        - ds0
        - ds1
      transaction-type: LOCAL
```

## 默认 Bean

- `DataSourceInitializer`：启动阶段预热并校验 `DataSource`
- `MongoDatabaseInitializer`：启动阶段探活并校验 `MongoTemplate`
- `MybatisPlusInterceptor`：默认注册分页、乐观锁、防全表更新拦截器
- `AuditFieldMetaObjectHandler`：默认时间审计字段填充处理器
- `shardingSphereDataSource`：启用分库分表时注册统一 Sharding 数据源

## 扩展点

- 业务项目统一使用 `getboot.database.*`
- 数据库增强统一配置根收敛到 `com.getboot.database.api.properties.DatabaseProperties`
- 数据源实现相关代码统一收敛在 `com.getboot.database.infrastructure.datasource.*`
- MongoDB 实现相关代码统一收敛在 `com.getboot.database.infrastructure.mongodb.*`
- MyBatis-Plus 实现相关代码统一收敛在 `com.getboot.database.infrastructure.mybatisplus.*`
- ShardingSphere 实现相关代码统一收敛在 `com.getboot.database.infrastructure.sharding.*`
- `getboot.database.enabled=true` 时会启用数据源预热、MyBatis-Plus 拦截器与审计字段处理器
- `getboot.database.datasource.*` 会桥接到底层 `spring.datasource.*`，但 `enabled` 与 `init.*` 仍保留在模块能力层
- `getboot.database.mongodb.*` 会桥接到底层 `spring.data.mongodb.*`，但 `enabled` 与 `init.*` 仍保留在模块能力层
- `getboot.database.mongodb.enabled=true` 且存在 `MongoTemplate` 时，会启用 MongoDB 初始化与启动后校验
- `getboot.database.mybatis-plus.*` 会桥接到底层 `mybatis-plus.*`
- `getboot.database.sharding.props.*` / `mode.*` / `rules.*` / `datasource.*` 会桥接到底层 `spring.shardingsphere.*`
- `reuse-bean-datasources=true` 时，会优先复用 Spring 容器中已有的底层 `DataSource` Bean，并按 `data-source-beans` 顺序组装
- 推荐在分库分表规则 YAML 中使用 `${...}` 占位符引用 Spring 环境属性，模块会在创建数据源前先解析占位符
- 当前模块没有额外抽出稳定 SPI，优先通过覆盖标准 Spring / MyBatis-Plus Bean 进行定制

## 已实现技术栈

- DataSource
- MongoDB
- MyBatis-Plus
- ShardingSphere

## 边界 / 补充文档

- 当前模块负责关系型数据库与 MongoDB 的接入增强与规则桥接，不抽象通用仓储模型，也不提供统一仓储 DSL
- 当前 MongoDB 范围只覆盖 `MongoTemplate` 启动探活、启动后连通性校验和 `spring.data.mongodb.*` 配置桥接，不额外抽象文档查询模型
- 如果启用 `getboot.database.sharding.enabled=true`，会基于 `rule-config` 指向的 YAML 规则文件装配 `shardingSphereDataSource`
- 与 `getboot-transaction` 联用时，默认要求 `getboot.database.sharding.transaction-type=LOCAL`，避免双重事务协调器冲突
- 可直接参考 `src/main/resources/getboot-database.yml.example`
- 可直接参考 `src/main/resources/getboot-database-sharding.yml.example`
- 可直接参考 `src/main/resources/getboot-database-shardingsphere-rule.yaml.example`
