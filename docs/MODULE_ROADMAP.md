# Module Roadmap

这份文档把下一批模块的边界先定下来，目标不是一次把实现做完，而是先把“该不该拆模块、模块到底收什么”说清楚。

## 设计约束

- 新模块继续遵循 `api -> spi -> support -> infrastructure.<tech>` 的分层，不把第三方 SDK 直接暴露给业务层
- 先收口统一前缀、默认 Bean 和稳定能力入口，再补具体厂商实现
- 每个模块只解决一类长期稳定的问题，不把临时业务诉求揉进公共层
- 文档先行，避免后续实现时又退回到“按 SDK 堆 starter”

## 建议落地顺序

1. `getboot-idempotency`
2. `getboot-storage`
3. `getboot-sms`
4. `getboot-search`
5. `getboot-ai`

补充：

- 消息重试、延迟消息、死信治理优先在 `getboot-mq` 模块内增强，不单拆新模块
- 邮件发送能力可作为 `getboot-mail` 后续候选模块，但优先级低于 `getboot-sms`

排序原则很简单：越接近高频基础能力、越容易形成统一抽象的模块，优先级越高；模型编排、检索增强这类变化更快的能力，放在后面落地。

更细评估见 [`docs/COMMON_CAPABILITY_ASSESSMENT.md`](./COMMON_CAPABILITY_ASSESSMENT.md)。

## 模块边界矩阵

| 模块 | 要解决的问题 | 首批实现方向 | 建议依赖 | 配置前缀 | 明确不做什么 |
| --- | --- | --- | --- | --- | --- |
| `getboot-idempotency` | 幂等键生成、请求去重、重复请求结果复用、TTL 管理 | `redis.redisson` / `database.jdbc` | `getboot-cache` / `getboot-coordination` | `getboot.idempotency.*` | 不承担分布式锁全部职责，不直接做 webhook 编排 |
| `getboot-storage` | 对象上传、下载、删除、预签名、元数据读写、桶路由 | `oss` / `minio` / `s3` | 无强制依赖；可选复用 `getboot-web` 上传模型 | `getboot.storage.*` | 不抽媒体处理、转码、审核、CDN 域名管理 |
| `getboot-search` | 搜索索引写入、查询请求构建、分页/高亮结果模型、索引模板初始化 | `elasticsearch` / `opensearch` | 无强制依赖 | `getboot.search.*` | 不把推荐、向量检索、RAG 编排混入传统搜索模块 |
| `getboot-ai` | 模型调用门面、提示词模板、Embedding / Rerank 接口、工具编排入口 | `openai` / `qwen` / `doubao` / `zhipu` / `embedding-store` | 可选依赖 `getboot-search` / `getboot-storage` | `getboot.ai.*` | 不在第一阶段直接做完整 Agent 平台或工作流系统 |
| `getboot-sms` | 短信模板发送、签名/模板路由、验证码短信、供应商降级 | `aliyun` / `tencent` / `huawei` | 无强制依赖 | `getboot.sms.*` | 不把邮件、站内信、Push 混成一个消息中心 |

## 每个模块最少应包含什么

### `getboot-idempotency`

- `api.annotation`：`@Idempotent`
- `api.model`：幂等结果、幂等状态、键生成上下文
- `spi`：幂等存储、键生成器、重复请求返回策略
- `support`：默认键生成、默认切面、默认结果序列化
- `infrastructure.redis` / `infrastructure.jdbc`：具体存储实现

### `getboot-storage`

- `api`：`StorageOperator`、对象描述、上传/下载请求模型
- `spi`：路径生成器、Bucket 路由器、元数据增强器
- `support`：默认对象键规则、统一异常包装
- `infrastructure.oss` / `infrastructure.minio` / `infrastructure.s3`

### `getboot-search`

- `api`：查询请求、分页结果、索引文档写入门面
- `spi`：查询构建器、索引名称解析器、结果映射器
- `support`：统一查询 DSL 包装、默认分页/高亮模型
- `infrastructure.elasticsearch` / `infrastructure.opensearch`

### `getboot-ai`

- `api.model`：聊天请求、补全结果、Embedding/Rerank 输入输出
- `api.prompt`：提示词模板与变量渲染模型
- `spi`：模型客户端、工具调用适配器、向量存储路由器
- `support`：默认提示词渲染、默认模型选择策略
- `infrastructure.<provider>`：供应商 SDK 适配

### `getboot-sms`

- `api`：发送请求、批量发送请求、发送回执
- `spi`：供应商客户端、模板变量渲染、签名路由
- `support`：验证码场景模板、失败重试策略
- `infrastructure.aliyun` / `infrastructure.tencent` / `infrastructure.huawei`

## 建议的第一阶段实现边界

- `getboot-idempotency`
  先做注解切面 + Redis 存储 + 重复请求直接返回缓存结果
- `getboot-storage`
  先做对象上传/删除/预签名 URL，不先抽分片上传编排
- `getboot-search`
  先做索引写入、基础查询和分页，不先抽 DSL 全量覆盖
- `getboot-ai`
  先做 Chat / Embedding / Rerank 三条稳定入口，不先做多 Agent
- `getboot-sms`
  先做单发、批量发送、验证码模板，不先做营销短信编排

## 后续执行建议

- 模块落地前，先补一份模块级 README 草案，再开始建目录
- 新模块进入仓库前，先在 `docs/MODULE_MAP.md` 补上定位、依赖和配置前缀
- 如果某个能力短期内仍然明显不稳定，就先留在模块文档，不急着抽成公共 `spi`
