# OpenAI 最小接入示例

这份示例只覆盖当前 `getboot-ai` 已稳定承载的 AI 能力：

- Chat：统一文本生成、摘要、改写、问答
- Embedding：统一文本向量化
- Rerank：基于 Embedding 相似度的结果重排

## Maven 依赖

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-ai</artifactId>
</dependency>
```

## 配置

```yaml
getboot:
  ai:
    enabled: true
    type: openai
    openai:
      enabled: true
      base-url: https://api.openai.com/v1
      api-key: sk-xxx
      default-chat-model: gpt-5-mini
      default-embedding-model: text-embedding-3-small
      default-reasoning-effort: low
      connect-timeout: 3s
      read-timeout: 30s
```

说明：

- `base-url` 默认就是 `https://api.openai.com/v1`，只有在走代理或兼容网关时才需要覆盖
- `default-chat-model` 用于 Chat 请求未显式指定模型时的默认值
- `default-embedding-model` 同时也是第一版 Rerank 的默认向量模型
- `default-reasoning-effort` 会透传到 `Responses API`

## Chat

```java
@Service
public class ArticleSummaryFacade {

    private final AiOperator aiOperator;

    public ArticleSummaryFacade(AiOperator aiOperator) {
        this.aiOperator = aiOperator;
    }

    public String summarize(String title, String content) {
        PromptTemplate template = new PromptTemplate();
        template.setContent("""
                请基于标题和正文生成 120 字中文摘要。
                标题：{{title}}
                正文：{{content}}
                """);
        template.setVariables(Map.of(
                "title", title,
                "content", content
        ));

        AiChatRequest request = new AiChatRequest();
        request.setInstructions("You are a concise Chinese summarization assistant.");
        request.setPromptTemplate(template);

        return aiOperator.chat(request).getContent();
    }
}
```

说明：

- `instructions` 适合放稳定角色约束
- 动态业务内容建议放到 `PromptTemplate`
- 当前内部通过 OpenAI `Responses API` 完成调用

## Embedding

```java
@Service
public class ArticleEmbeddingFacade {

    private final AiOperator aiOperator;

    public ArticleEmbeddingFacade(AiOperator aiOperator) {
        this.aiOperator = aiOperator;
    }

    public List<Double> embedTitle(String title) {
        AiEmbeddingRequest request = new AiEmbeddingRequest();
        request.setInputs(List.of(title));

        AiEmbeddingResponse response = aiOperator.embed(request);
        return response.getItems().get(0).getVector();
    }
}
```

说明：

- 一个请求可以同时传多段文本
- 返回值里的 `items[index]` 与输入文本顺序保持一致

## Rerank

```java
@Service
public class SearchRerankFacade {

    private final AiOperator aiOperator;

    public SearchRerankFacade(AiOperator aiOperator) {
        this.aiOperator = aiOperator;
    }

    public AiRerankResponse rerank(String query, List<String> documents) {
        AiRerankRequest request = new AiRerankRequest();
        request.setQuery(query);
        request.setDocuments(documents);
        request.setTopK(5);

        return aiOperator.rerank(request);
    }
}
```

说明：

- 第一版 `Rerank` 不是走厂商专有 rerank endpoint
- 当前实现会先对 `query + documents` 做 Embedding，再按余弦相似度降序返回
- 这样做的目标是先稳定 `AiOperator.rerank(...)` 这层能力入口，而不是把短期波动较大的供应商接口直接暴露给业务层

## 常见扩展点

如果你需要改提示词模板渲染规则，可直接覆盖默认渲染器：

```java
@Component
public class DemoPromptTemplateRenderer implements PromptTemplateRenderer {

    @Override
    public String render(PromptTemplate template) {
        return template.getContent();
    }
}
```

如果你需要接其他模型供应商，可直接覆盖 `AiModelClient`。
