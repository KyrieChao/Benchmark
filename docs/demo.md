# Failure Benchmark 指南（Benchmark 该怎么做）

本仓库用于评估 Failure 在不同使用模式下的性能表现，并与对照方案（Hibernate Validator / 手写 if-throw / AOP 校验等）做**语义对齐**的对比。核心原则：**结果可复现、对比公平、结论可解释**。

## 1. 设计目标

- **可复现**：任何人拉取仓库后，在同等环境下能复现相近结果，并能看到原始数据（非截图）。
- **可解释**：每个 case 明确“做了什么工作、构造了哪些对象、失败在哪一条规则”。
- **语义对齐**：对比项完成同等语义（Fail-Fast/Collect-All、是否构建 violation、是否消息插值/i18n）。
- **可回归**：能在 CI 中定期跑“核心子集”，发现性能回退。

非目标：
- 追求绝对数值最漂亮；Benchmark 的价值在趋势与公平对比。
- 用微基准（JMH）去证明端到端吞吐；端到端属于集成压测范畴。

---

## 2. 仓库结构建议

建议把“微基准”和“集成压测”彻底分层，避免混用单位与叙事。

- 当前仓库落地（单模块 Maven）：
    - `src/main/java/com/chao/benchmark/demo/`
        - 基准 case 与 runner（JMH 与非 JMH 的快速 smoke 允许共存）
        - 对外统一以 `com.chao.failfast.*`（依赖 `failure-spring-boot-starter`）做对比与语义对齐
    - `docs/`
        - 方法学、case 定义、对照方案说明、如何解读

- `benchmarks-jmh/`
    - 只放 JMH micro-benchmarks（ns/op 或 ops/s + alloc/op）
    - 输出 JSON/CSV 结果
- `benchmarks-integration/`
    - Spring Boot 集成对比（HTTP、AOP、序列化等）
    - 输出吞吐（req/s）、延迟分位（p50/p95/p99）、GC 指标
- `scripts/`
    - 一键运行脚本（本地/CI）
    - 报告生成脚本（把 JSON/CSV 转图表与 Markdown）
- `reports/`
    - 自动生成的报告（按日期/commit）
    - 原始数据引用（JSON/CSV 的链接或归档）
- `docs/`
    - 方法学、case 定义、对照方案说明、如何解读

---

## 3. 基准方法学（强制规范）

### 3.1 微基准必须使用 JMH

微基准用于回答“某个 API/路径的单位成本”，例如：
- Failure.begin() 链式校验：valid/invalid
- Failure.strict() 收集所有错误：规则数变化、失败位置变化
- @Validate AOP 的开销：无校验/轻校验/重校验
- TypedValidator 分发：单类型 vs 多类型，命中率、继承层级

强制要求：
- 输入来自 `@State`，不能被常量折叠。
- 输出必须返回给 JMH 或 `Blackhole.consume`，防止死码消除（DCE）。
- 把“构建成本”与“执行成本”分开：
    - `@Setup(Level.Trial)` 做预热、缓存构建、对象准备
    - 基准方法只测“执行路径”
- 结果必须输出：
    - score（ns/op 或 ops/s）
    - score error（误差）
    - `gc.alloc.rate.norm`（alloc/op）等分配指标（使用 JMH profiler）

推荐配置（示例口径）：
- JDK：17（注明完整版本）
- Fork：3
- Warmup：5 × 1s
- Measurement：10 × 1s
- JVM 参数：明确记录（特别是 GC、TieredCompilation 等）

### 3.2 集成压测用于端到端结论

集成压测回答：
- 在典型 Spring Boot 应用上下文中，Failure 与对照方案的吞吐与延迟分位
- AOP 在真实 Controller/Service 调用链中的开销
- 高并发下内存分配与 GC 压力（这是生产关注点）

强制要求：
- 明确压测工具、并发模型、请求体大小、响应体结构、网络环境。
- 结果必须输出：
    - 吞吐（req/s）
    - 延迟分位（p50/p95/p99）
    - GC 指标（次数/暂停时间/分配率）
    - 关键 JVM 参数与机器信息

---

## 4. 对比必须“语义对齐”（最常被质疑的点）

任何对比结论必须先回答：**两边是否做了同等工作？**

### 4.1 Failure vs Hibernate Validator（HV）

至少提供三组对齐对比，并分别解释：

1) **Fail-Fast 对齐**
- HV：启用 fail-fast（说明如何配置）
- Failure：begin()/fast=true

2) **Collect-All 对齐**
- HV：默认收集 violations
- Failure：strict()/failAll()

3) **完整语义对齐**
   必须说明是否包含：
- Violation 对象构建
- property path 构建
- message interpolation（含 i18n）
  这些往往是 HV 成本大头，不写清楚“快 N 倍”没有说服力。

### 4.2 Failure strict vs 手写 if 链

明确手写链是否也做了：
- 错误对象构造（List 追加）
- 错误码/错误消息的组装
- 是否收集全部错误还是遇错即停
  否则对比会天然偏向“少做事的那边”。

### 4.3 @Validate（AOP）开销测量

AOP 开销要拆成：
- 仅切面空转（无校验器）
- 轻量校验器（少量条件判断）
- 重量校验器（多规则/集合/正则等）
  并说明是否包含 Spring 容器取 Bean、反射、缓存命中率等因素。

---

## 5. Case 设计：让数据“可解释”

每个基准 case 必须声明以下参数（建议放到统一的 `CaseDefinition` 文档里）：

- 模式：fail-fast / fail-strict / AOP / Bean Validation
- 规则条数：N
- 失败位置：第 1 条 / 中间 / 最后一条（这对 fail-fast 影响巨大）
- 输入类型：String/Number/Collection/Object/嵌套对象
- 是否构建错误集合：是/否
- 是否 i18n：是/否（message key vs resolved text）
- 是否包含异常堆栈：是/否（生产默认通常不应填充栈）
- 基准单位：ns/op（micro）或 req/s + p99（integration）

避免“规则越多反而越快”这类容易被认为是伪影的结论；一旦出现必须做解释与复核。

---

## 6. 结果输出与报告规范

### 6.1 原始数据优先

每次运行必须产出：
- `results.json`（JMH 原始输出）
- `results.csv`（可选）
- 集成压测原始日志/报告

### 6.2 报告模板（建议）

报告必须包含：
- 环境：CPU/内存/OS/JDK/JVM 参数
- Failure 版本号、Benchmark commit hash
- 运行参数：fork/warmup/measurement、线程数
- 结果：表格（score ± error、alloc/op）+ 图表
- 结论：明确适用边界（何时快、何时不明显、何时不建议用）
- 风险提示：对比是否完全语义对齐、哪些结果仅供趋势参考

---

## 7. CI 与性能回归

建议提供两类 CI 任务：

- **PR 轻量任务（快速）**
    - 只跑“核心子集”基准（少量 case，固定参数）
    - 产出结果并与基线对比
    - 回归阈值：例如 ops/s 下降 > X% 或 alloc/op 上升 > Y%

- **Nightly/Weekly 全量任务（完整）**
    - 跑全量 case + 集成压测
    - 自动生成报告并发布到 `reports/` 或 Release Artifact

---

## 8. 常见基准陷阱清单（务必规避）

- 把输入写成常量或编译期可推导 → 结果被常量折叠
- 忘记消费输出 → 死码消除导致“看起来极快”
- 在 `@Benchmark` 内构建大量对象/初始化缓存 → 把构建成本当执行成本
- 用手写循环替代 JMH → 极易被 JIT 优化误导
- 多线程用 ns/op 解读吞吐 → 容易误读，应同时给 ops/s 与延迟分位
- 只贴图不贴原始数据 → 外部无法验证

---

## 9. 如何解读结果（给读者的正确心智）

- 微基准（JMH）看：趋势 + alloc/op + 误差区间；不要把极端纳秒级差异当成生产差异。
- 集成压测看：p95/p99 + GC + 吞吐；生产体验往往由尾延迟与 GC 决定。
- “快 N 倍”的结论必须写明：语义对齐方式、失败率假设、规则数与失败位置。

---

## 10. 贡献与扩展

欢迎贡献新的 benchmark case。提交新 case 必须包含：
- Case 定义（规则数、失败位置、语义说明）
- JMH 结果（至少一次运行的原始 JSON）
- 对照方案说明（为何公平）

---

如果你希望这份文档更贴合你现有 Benchmark 仓库，我也可以按你现在报告里已有的四大比较点（Failure vs @Valid、Fail-Strict vs 手写链、@Validate AOP、并发下 GC/alloc）把“Case 列表”写成一份更具体的清单（每个 case 的输入、规则、失败率、预期指标）。
