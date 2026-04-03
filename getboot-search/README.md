# getboot-search

搜索 starter，当前第一版先把最常见、最适合统一的那层收口：

- 提供统一 `SearchOperator` 门面，覆盖索引写入、文档删除、基础查询和分页结果映射
- 收口逻辑索引名解析、默认分页大小、排序与高亮请求模型
- 先内置 `Elasticsearch` 实现，避免业务代码直接耦合 REST DSL 和客户端细节

## 作用

适合这几类场景：

- 业务里需要统一维护文章、商品、订单这类索引写入和检索入口
- 不想在每个项目里重复拼 `_search` DSL、分页、排序和高亮字段结构
- 想把逻辑索引名、索引前缀和节点配置统一放到配置层，而不是散落在工具类里

## 接入方式

业务项目继承父 `pom` 后，引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-search</artifactId>
</dependency>
```

当前第一版只内置 `Elasticsearch` 实现，配置好节点地址后即可直接使用。

## 前置条件

- 当前没有强制配套 `getboot-*` 模块
- 需要准备可访问的 Elasticsearch 集群或兼容 REST API 的搜索服务

## 目录约定

- `api.*`
  对外稳定能力入口、请求模型、响应模型和配置
- `spi`
  逻辑索引名解析扩展点
- `support`
  默认索引名规则和搜索辅助方法
- `infrastructure.elasticsearch.*`
  Elasticsearch 客户端装配和 REST 请求适配

## 配置示例

```yaml
getboot:
  search:
    enabled: true
    type: elasticsearch
    default-index-prefix: demo
    default-page-size: 20
    max-page-size: 100
    index-aliases:
      article: article_v1
      order: order_current
    elasticsearch:
      enabled: true
      uris:
        - http://127.0.0.1:9200
      path-prefix:
      username:
      password:
      api-key:
      connect-timeout: 2s
      socket-timeout: 5s
      default-headers:
        X-Request-Source: getboot
```

- `default-index-prefix`
  默认索引名前缀，例如 `demo-article`
- `index-aliases`
  逻辑索引名到物理索引名的映射，例如 `article -> article_v1`
- `default-page-size` / `max-page-size`
  统一分页默认值和保护上限
- `elasticsearch.uris`
  Elasticsearch 节点地址列表
- `elasticsearch.api-key`
  配置后优先使用 API Key；未配置时可继续使用 `username/password`

## 使用示例

```java
@Service
public class ArticleSearchService {

    private final SearchOperator searchOperator;

    public ArticleSearchService(SearchOperator searchOperator) {
        this.searchOperator = searchOperator;
    }

    public void saveArticle(String articleId, Map<String, Object> document) {
        SearchIndexRequest request = new SearchIndexRequest();
        request.setIndexName("article");
        request.setDocumentId(articleId);
        request.setRefresh(true);
        request.setDocument(document);
        searchOperator.index(request);
    }

    public SearchPageResponse<ArticleDocument> searchArticles(String keyword) {
        SearchQueryRequest request = new SearchQueryRequest();
        request.setIndexName("article");
        request.setKeyword(keyword);
        request.setKeywordFields(List.of("title", "content"));
        request.setHighlightFields(List.of("title"));
        request.setPageNo(1);
        request.setPageSize(10);

        SearchSortField sortField = new SearchSortField();
        sortField.setFieldName("publishTime");
        sortField.setOrder(SearchSortOrder.DESC);
        request.setSortFields(List.of(sortField));
        return searchOperator.search(request, ArticleDocument.class);
    }
}
```

## 默认 Bean

- `SearchIndexNameResolver`
  默认实现为 `DefaultSearchIndexNameResolver`
- `RestClient`
  当 `getboot.search.elasticsearch.uris` 存在时自动注册
- `ElasticsearchRestGateway`
  默认实现为 `DefaultElasticsearchRestGateway`
- `SearchOperator`
  当容器内存在 `ElasticsearchRestGateway` 时，默认实现为 `ElasticsearchSearchOperator`

## 扩展点

- 可注册 `SearchIndexNameResolver` Bean，自定义逻辑索引名到物理索引名的解析规则
- 可直接覆盖 `SearchOperator` Bean，自定义查询 DSL 或接入其他搜索实现

## 已实现技术栈

- `elasticsearch`

## 边界 / 补充文档

- 当前第一版只覆盖索引写入、文档删除、基础查询、分页、排序和高亮，不承接索引模板管理、聚合分析、Suggest、向量检索和推荐编排
- 当前查询模型只覆盖最常见的关键字、多字段匹配和精确过滤，不追求 DSL 全量透传
- 当前只内置 Elasticsearch 实现，OpenSearch 兼容和更复杂查询编排后续再补
- 配置示例可直接参考 [`getboot-search.yml.example`](./src/main/resources/getboot-search.yml.example)
