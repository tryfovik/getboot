# getboot-ai

AI starter，当前第一版先把业务里最容易重复接、也最适合统一抽象的那层收口：

- 提供统一 `AiOperator` 门面，覆盖 Chat、Embedding 和 Rerank 三个稳定入口
- 收口提示词模板渲染、默认模型配置和 OpenAI 鉴权/超时/请求头配置
- 当前内置 `OpenAI` 聊天与 Embedding 实现，Rerank 第一版基于 Embedding 相似度完成，不把厂商专有接口直接暴露到业务层

## 作用

适合这几类场景：

- 业务里需要统一做内容生成、摘要、问答补全这类 Chat 调用
- 想把 Query、文档向量化和结果重排收口成稳定门面，而不是每个项目各自拼 HTTP 请求
- 需要在公共层统一维护模型名、API Key、超时和提示词模板，不想散落在工具类和业务服务里

## 接入方式

业务项目继承父 `pom` 后，引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-ai</artifactId>
</dependency>
```

当前第一版只内置 `OpenAI` 实现，配置好 API Key 后即可直接使用。

## 前置条件

- 当前没有强制配套 `getboot-*` 模块
- 需要准备可访问的 OpenAI 兼容接口地址和 API Key
- 如果要把 Rerank 结果继续接搜索或知识库，可按需与 `getboot-search` / `getboot-storage` 组合

## 目录约定

- `api.*`
  对外稳定能力入口、请求模型、响应模型、提示词模板和配置
- `spi`
  模型客户端与提示词渲染扩展点
- `support`
  默认提示词模板渲染、向量相似度计算和默认门面实现
- `infrastructure.openai.*`
  OpenAI 请求网关、客户端适配和自动配置

## 配置示例

```yaml
getboot:
  ai:
    enabled: true
    type: openai
    openai:
      enabled: true
      base-url: https://api.openai.com/v1
      api-key: sk-xxx
      organization:
      project:
      default-chat-model: gpt-5-mini
      default-embedding-model: text-embedding-3-small
      default-reasoning-effort: low
      connect-timeout: 3s
      read-timeout: 30s
      default-headers:
        X-Request-Source: getboot
```

- `type`
  当前实现类型，第一版固定为 `openai`
- `openai.base-url`
  OpenAI 兼容接口地址，默认 `https://api.openai.com/v1`
- `openai.default-chat-model`
  Chat 默认模型名，请求未指定模型时使用
- `openai.default-embedding-model`
  Embedding 默认模型名，也作为第一版 Rerank 的默认向量模型
- `openai.default-reasoning-effort`
  Responses API 的默认 reasoning effort

## 使用示例

```java
@Service
public class ArticleAiService {

    private final AiOperator aiOperator;

    public ArticleAiService(AiOperator aiOperator) {
        this.aiOperator = aiOperator;
    }

    public String summarizeArticle(String title, String content) {
        PromptTemplate template = new PromptTemplate();
        template.setContent("请基于标题和正文生成 120 字中文摘要。标题：{{title}}。正文：{{content}}");
        template.setVariables(Map.of("title", title, "content", content));

        AiChatRequest request = new AiChatRequest();
        request.setInstructions("You are a concise Chinese summarization assistant.");
        request.setPromptTemplate(template);

        return aiOperator.chat(request).getContent();
    }

    public AiRerankResponse rerankDocuments(String query, List<String> documents) {
        AiRerankRequest request = new AiRerankRequest();
        request.setQuery(query);
        request.setDocuments(documents);
        request.setTopK(5);
        return aiOperator.rerank(request);
    }
}
```

## 默认 Bean

- `PromptTemplateRenderer`
  默认实现为 `DefaultPromptTemplateRenderer`
- `HttpClient`
  当 `getboot.ai.openai.api-key` 存在时自动注册
- `OpenAiRestGateway`
  默认实现为 `DefaultOpenAiRestGateway`
- `AiModelClient`
  当启用 `openai` 实现且存在 API Key 时，默认实现为 `OpenAiAiModelClient`
- `AiOperator`
  当容器内存在 `AiModelClient` 时，默认实现为 `DefaultAiOperator`

## 扩展点

- 可注册 `PromptTemplateRenderer` Bean，自定义提示词模板渲染规则
- 可直接覆盖 `AiModelClient` Bean，自定义模型供应商或请求协议
- 可直接覆盖 `AiOperator` Bean，自定义 Chat / Embedding / Rerank 编排逻辑

## 已实现技术栈

- `openai`

## 最小示例

更多示例见：

- [docs/OPENAI_MINIMAL_INTEGRATION.md](docs/OPENAI_MINIMAL_INTEGRATION.md)

## 边界 / 补充文档

- 当前第一版只覆盖 Chat、Embedding 和基于 Embedding 相似度的 Rerank，不承接工具调用、工作流、多 Agent 和完整知识库编排
- 当前 Chat 接口基于 OpenAI `Responses API`，Embedding 接口基于 `Embeddings API`
- 当前 Rerank 没有引入厂商专有 rerank endpoint，而是统一走 Embedding 相似度，保证能力层接口稳定
- 配置示例可直接参考 [`getboot-ai.yml.example`](./src/main/resources/getboot-ai.yml.example)
- OpenAI 最小接入示例见 [`docs/OPENAI_MINIMAL_INTEGRATION.md`](./docs/OPENAI_MINIMAL_INTEGRATION.md)
