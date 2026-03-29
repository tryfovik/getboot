# getboot-database

数据访问增强 starter，提供数据源预热、MyBatis-Plus 拦截器、审计字段填充与 ShardingSphere 分库分表接入能力。

## 作用

- 提供数据源启动预热与连通性校验
- 提供 MyBatis-Plus 分页、乐观锁、防全表更新拦截器
- 提供默认审计字段自动填充处理器
- 提供 ShardingSphere 分库分表接入与规则文件占位符解析

## 目录约定

- `api.properties`：对外稳定配置模型
- `support.datasource`：内部数据源初始化辅助能力
- `infrastructure.datasource.*`：数据源预热与初始化
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
- `MybatisPlusInterceptor`：默认注册分页、乐观锁、防全表更新拦截器
- `AuditFieldMetaObjectHandler`：默认时间审计字段填充处理器
- `shardingSphereDataSource`：启用分库分表时注册统一 Sharding 数据源

## 扩展点

- 业务项目统一使用 `getboot.database.*`
- 数据源原生配置统一收敛到 `getboot.database.datasource.*`
- MyBatis-Plus 配置统一使用 `getboot.database.mybatis-plus.*`
- 分库分表配置统一使用 `getboot.database.sharding.*`
- 数据库增强统一配置根收敛到 `com.getboot.database.api.properties.DatabaseProperties`
- 数据源实现相关代码统一收敛在 `com.getboot.database.infrastructure.datasource.*`
- MyBatis-Plus 实现相关代码统一收敛在 `com.getboot.database.infrastructure.mybatisplus.*`
- ShardingSphere 实现相关代码统一收敛在 `com.getboot.database.infrastructure.sharding.*`
- `getboot.database.enabled=true` 时会启用数据源预热、MyBatis-Plus 拦截器与审计字段处理器
- `getboot.database.sharding.enabled=true` 时会基于 `rule-config` 指向的 YAML 规则文件装配 ShardingSphere `DataSource`
- `reuse-bean-datasources=true` 时，会优先复用 Spring 容器中已存在的底层 `DataSource` Bean，并按 `data-source-beans` 顺序组装
- 推荐在规则 YAML 中通过 `${...}` 占位符引用 Spring 环境属性，当前模块会在创建数据源前先解析占位符
- 若同时启用 `getboot-transaction` 的 Seata 集成，默认要求 `getboot.database.sharding.transaction-type=LOCAL`，避免双重事务协调器冲突
- 当前模块没有额外抽出稳定 SPI，优先通过覆盖标准 Spring / MyBatis-Plus Bean 进行定制
- 可直接参考 `src/main/resources/getboot-database-sharding.yml.example` 与 `src/main/resources/getboot-database-shardingsphere-rule.yaml.example`

## 已实现技术栈

- DataSource
- MyBatis-Plus
- ShardingSphere
