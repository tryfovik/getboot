# 开发与维护规范

这份文档面向仓库维护者、模块开发者，以及在仓库内协作的 AI 编码代理。

目标不是把规则写得很满，而是让任何人进入仓库后都能快速知道：

- 新能力应该怎么设计
- 代码应该放到哪里
- 文档应该怎么写
- 第一次接触仓库的人应该先看什么
- 一个变更做到什么程度才算完成

## 1. 先看什么

推荐阅读顺序：

1. 根 `README.md`
2. `docs/MODULE_MAP.md`
3. `docs/MODULE_ROADMAP.md`
4. `docs/DDD_PACKAGE_RULES.md`
5. 目标模块自己的 `README.md`
6. `docs/TODO.md`

## 2. 仓库定位

`getboot` 是一组按能力拆分的 Spring Boot 基础模块，不是后台脚手架，也不是技术栈演示仓库。

它还承担一个很实际的组织角色：在没有专门基础架构团队，或者基础架构团队覆盖不到所有业务线时，把统一响应、异常、Trace、缓存、RPC、MQ 这类跨项目基础规范收口到公共层，而不是继续散落在业务仓库里。

这里的核心要求只有两个：

- 外部业务项目按需引入，不强制全家桶
- 能力层接口尽量稳定，实现层可以继续演进

## 3. 开发原则

### 3.1 能力优先，不是技术栈优先

- 模块名表达能力，不表达技术栈
- 配置前缀表达能力，不表达技术栈
- 后续新增实现时，优先扩 `infrastructure` 子树，而不是推翻 `api`

示例：

- `getboot-lock` 是能力名
- 当前实现可以是 `infrastructure.redis.redisson`
- 后续也可以增加 `infrastructure.database.jdbc`
- 再后续也可以增加 `infrastructure.zookeeper.curator`

### 3.2 分层要稳定

常规模块统一遵循：

- `api`
- `spi`
- `support`
- `infrastructure`

Foundation 模块允许更轻量，但也必须保证“稳定层和实现层”一眼能区分：

- `getboot-support`：`api + infrastructure`
- `getboot-exception`：`api.code + api.exception`
- `getboot-web`：`api.request + api.response + infrastructure`

### 3.3 外部接口尽量不跟着实现波动

- 业务方依赖 `api` / `spi`
- `support` 只作为内部默认实现和辅助工具
- 实现细节、SDK 接入、自动配置、环境适配都留在 `infrastructure`
- 不把底层 SDK 类型直接抛给业务代码
- 除非模块 README 明确说明，否则外部项目不应直接依赖 `support` / `infrastructure`

### 3.4 注释与日志语言约定

- 代码注释、类注释、字段注释、方法注释默认使用中文，优先降低仓库维护者的阅读门槛
- 不能提交没有中文注释的类、字段和手写方法；如果是纯数据载体类，至少要有类注释和字段注释
- 日志、异常消息、对外返回的技术性错误描述优先使用英文，便于统一检索、国际化和跨团队排障
- 如果某段注释必须引用第三方术语，先用中文说清语义，再保留必要英文名词

### 3.5 Lombok 约定

- 仓库统一使用根目录 [`lombok.config`](./lombok.config) 作为 Lombok 配置入口，不在模块内各自维护零散配置
- `request`、`response`、`properties`、`dto`、`item` 这类纯数据载体，默认直接使用 Lombok，不再重复手写 getter / setter / equals / hashCode / toString
- 纯可变载体默认优先使用 `@Data`；只有需要限制方法暴露范围、定制只读行为，或存在明确业务逻辑时，才退回 `@Getter` / `@Setter` 或手写方法
- 带有显式拷贝、防御性赋值、参数归一化的 setter 可以保留手写实现，但其余访问器仍应交给 Lombok 生成
- 使用 Lombok 的纯载体类，不要求为 Lombok 生成的 getter / setter 重复补方法注释；由类注释和字段注释承担语义说明
- 模块一旦使用 Lombok 注解，模块 `pom.xml` 里必须显式声明 `org.projectlombok:lombok` 的 `provided` 依赖，避免 IDE 和 Maven 表现不一致

### 3.6 本地环境约定

- 仓库统一以 `Java 17+` 为编译基线；`pom.xml` 已显式使用 `release 17`
- 不只看 `java -version`，还要看 `mvn -version` 实际绑定的 JDK，因为 Maven 走的是 `JAVA_HOME`
- 如果 Maven 仍然挂在 JDK 8，模块测试会直接报 `invalid target release: 17`
- PowerShell 下临时切换 JDK 时，优先在当前命令前显式设置 `JAVA_HOME` 和 `Path`，不要靠系统里多个 Java 安装碰运气

PowerShell 示例：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:Path='C:\Program Files\Java\jdk-21.0.10\bin;' + $env:Path
mvn -version
mvn "-Dmaven.repo.local=D:\project\getboot\.m2-repo" -pl <module> -am test
```

## 4. 依赖方向

总体依赖方向固定为：

`Foundation -> Infrastructure Capability -> Cross-Cutting / Communication -> Ecosystem`

约束很简单：

- Foundation 保持轻量
- Infrastructure Capability 作为底座
- Cross-Cutting / Communication 尽量复用底座
- Ecosystem 面向具体生态，不反向定义全仓公共规范

更细的依赖说明看 `docs/MODULE_MAP.md`。

## 5. 新增或修改代码怎么落位

改代码前先问自己四个问题：

1. 去掉技术栈词汇，这个类还成立吗
2. 未来换一套实现时，这个类还会复用吗
3. 业务方是否应该直接依赖这个类
4. 这个类是否明显属于 Spring / SDK / 中间件接入细节

落位规则：

- 业务方应直接依赖的接口、模型、注解：放 `api`
- 模块扩展点：放 `spi`
- 技术栈专属扩展点：放 `spi.<tech>`
- 默认实现、内部 helper、内部 facade：放 `support`
- 自动配置、监听器、适配器、环境映射、SDK 接入：放 `infrastructure`

## 6. 文档规范

文档必须和代码一样讲究可读性，最低要求是“不读代码也知道怎么接”。

### 6.1 根目录只保留两个主文档

- `README.md`
  面向对外使用者，回答“为什么要用、先怎么接、模块怎么选、接下来应该看什么”
- `DEVELOPMENT.md`
  面向内部开发者，回答“怎么开发、怎么写文档、怎么验收”

根 `README.md` 至少要覆盖：

- 首页第一屏先回答“为什么现在值得继续看”，而不是只写中性仓库介绍
- 读者正在重复什么痛点，最好在开头直接说透
- 谁适合继续看、谁不适合继续看
- 接入后立刻少做什么，最好有一组高密度结论
- 父 `pom` 的接入方式
- 模块选型路径或组合建议
- 至少一组可复制的最小接入示例
- 这个仓库统一了哪些跨团队公共规范
- 新人第一次接仓库时建议怎么读、怎么接
- 根文档、技术参考、模块 README 之间的阅读顺序

### 6.2 `docs/` 只放技术参考和路线图

`docs/` 可以保留：

- 架构地图
- 包结构规则
- 路线图 / TODO
- 历史专项清单

不要再放“索引页的索引页”或和根文档重复的一套入口说明。

### 6.3 模块文档默认只有一个 README

每个 `getboot-*` 模块默认维护一个 `README.md`，优先把这些内容写全：

1. 这个模块解决什么问题
2. 业务项目怎么引入
3. 前置条件或配套模块是什么
4. 目录约定
5. 配置项是什么
6. 默认会注册什么 Bean
7. 有哪些扩展点
8. 当前不承诺什么边界

推荐直接按下面这组标题组织内容：

1. `作用`
2. `接入方式`
3. `前置条件`
4. `目录约定`
5. `配置示例`
6. `默认 Bean`
7. `扩展点`
8. `已实现技术栈`
9. `边界 / 补充文档`

没有配置项、没有额外前置条件或没有公开扩展点时，不要硬编内容，直接明确写“当前没有强制配置项”“当前没有额外前置条件”或“当前没有额外 SPI”。

专项能力确实足够大时，才允许继续拆子文档。拆出去的子文档也必须明确：

- 它解决哪一段接入问题
- 它和主 README 的关系
- 读者先看主 README 还是先看专项文档

### 6.4 文档写作要求

- 先说价值，再说细节
- 先给读者结论，再给补充说明
- 根 README 开头优先写痛点、收益和读者筛选，不要一上来只写“这是一个什么仓库”
- 例子尽量可复制，不要写只适合作者自己理解的术语
- 不把仓库文档写成“纯导航页”或“纯规则堆叠页”
- 尽量让第一次接触仓库的人也能沿着 README 走通最小接入路径
- 一旦接口、配置、默认 Bean、目录结构变化，README 必须同步
- 文档里提到的 Bean、SPI、配置前缀，必须能在代码里找到对应实现
- 文档必须写清楚业务项目该引哪个 `artifactId`
- 文档必须写清楚配置前缀是 `@ConfigurationProperties` 还是别名桥接
- 如果模块依赖底座能力，要明确说明“是否需要额外引模块”还是“只需要准备配置”
- 不把第三方 starter 的默认行为误写成 `getboot-*` 自己提供的能力

## 7. 开发流程

### 7.1 改代码前

- 先读目标模块 `README.md`
- 先确认当前目录是否符合 `docs/DDD_PACKAGE_RULES.md`
- 先判断这次是在补能力层，还是在补实现层

### 7.2 改代码时

- 优先保持外部接口稳定
- 优先新增实现子树，不轻易改能力层语义
- 如果移动文件，要同步更新 imports、自动配置、README、示例配置和根文档

### 7.3 改代码后

- 清理空目录和历史残留
- 搜索旧包名前缀是否还在
- 至少执行模块级 Maven 验证
- 如果变更跨模块，再做更大范围验证
- 如果改了文档结构，要顺手检查失效链接

### 7.4 提交规范

- 一个提交只解决一类清晰问题，不把无关改动揉进同一个 commit
- 先改文档或目录收敛，再做下一批能力扩展时，应该拆成多个 commit，保持历史可回看
- 改动涉及代码时，提交前至少确认对应模块已经过本地 Maven 验证；纯文档改动也要检查链接、标题和引用是否失效
- 文档变更如果改变了接入路径、配置前缀、默认 Bean 或模块边界，必须与代码一起进同一个 commit，不允许文档长期滞后
- 提交消息优先使用 `feat:`、`fix:`、`refactor:`、`docs:` 这类前缀，标题直接写清本次变更主题
- 如果一次工作分多轮推进，保持“先提交已完成的小闭环，再继续下一轮”，不要把长时间未完成的混合改动一直堆在工作区
- 提交前先看 `git diff --stat` 和 `git status`，确认没有把临时文件、格式化噪音或无关改动一起带上

## 8. 验证约定

常用命令：

```bash
mvn -q -o -pl <module> -am compile
mvn -q -Dmaven.repo.local=.m2 -pl <module> -am test
```

如果本机装了多个 JDK，先执行 `mvn -version`，确认 Maven 绑定的是 `Java 17+`。

默认策略：

- 小改动至少过模块级 `compile`
- 有行为变化时优先补模块级 `test`
- 不把每次小改动都升级成全仓大回归

## 9. 完成标准

一个改动可以认为完成，至少满足：

- 目录表达清晰
- 对外接口没有被无意义破坏
- 文档已同步
- README 没有过期
- Maven 验证已通过

如果是文档型改动，再额外检查：

- 根目录是否仍然只保留 `README.md` 和 `DEVELOPMENT.md` 两个主入口
- `docs/` 是否只保留技术参考、路线图和专项清单
- 模块 README 是否足够支撑接入，不需要读代码才能知道怎么用
- 第一次接触仓库的人，是否能只靠文档完成“父 `pom` -> 模块选择 -> 配置接入”这条路径

## 10. 相关参考

- 模块地图：`docs/MODULE_MAP.md`
- 包结构规则：`docs/DDD_PACKAGE_RULES.md`
- 风格治理清单：`docs/MODULE_STYLE_CHECKLIST.md`
- 路线图：`docs/TODO.md`
