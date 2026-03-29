# getboot-job

任务调度 starter，当前提供 XXL-JOB 执行器自动配置与管理端客户端能力。

## 作用

- 自动装配 XXL-JOB 执行器
- 提供管理端客户端，便于任务增删改查
- 暴露执行器和管理端客户端定制扩展点

## 目录约定

- `api.properties`：对外稳定配置模型
- `spi.xxl`：XXL-JOB 专属扩展点
- `support`：内部工具与辅助类
- `infrastructure.xxl.*`：XXL-JOB 实现与自动装配

## 配置示例

```yaml
getboot:
  job:
    enabled: true                    # 是否启用 XXL-JOB 自动配置
    xxl:
      access-token: ""               # XXL-JOB 访问令牌，没有可留空
      admin:
        addresses: http://127.0.0.1:8080/xxl-job-admin  # 管理端地址
        username: admin              # 管理端用户名
        password: 123456             # 管理端密码
      executor:
        app-name: demo-job           # 执行器名称
        address: ""                  # 执行器注册地址，留空时由 XXL-JOB 自行推断
        ip: 127.0.0.1                # 执行器 IP
        port: 9999                   # 执行器端口
        log-path: ./logs/xxl-job     # 执行器日志目录
        log-retention-days: 30       # 日志保留天数
```

## 默认 Bean

- `XxlJobSpringExecutor`：默认 XXL-JOB 执行器
- `XxlJobAdminClient`：默认 XXL-JOB 管理端客户端

## 扩展点

- 业务项目统一使用 `getboot.job.*`
- XXL-JOB 实现级配置统一收敛到 `getboot.job.xxl.*`
- 模块统一配置根收敛到 `com.getboot.job.api.properties.JobProperties`
- XXL-JOB 扩展点统一收敛到 `com.getboot.job.spi.xxl.*`
- XXL-JOB 实现相关代码统一收敛到 `com.getboot.job.infrastructure.xxl.*`
- 当前模块内部实现基于 XXL-JOB，后续如果新增 Quartz、ElasticJob 等调度技术栈，仍沿用 `getboot-job` 作为统一任务调度模块
- 可通过注册 `XxlJobExecutorCustomizer` Bean 参与执行器初始化
- 可通过注册 `XxlJobAdminClientConfigurer` Bean 在客户端创建前调整管理端地址、账号、密码与执行器应用名

## 已实现技术栈

- XXL-JOB
