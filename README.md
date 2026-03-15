# 📊 Failure 性能基准测试

> 用于 [Failure](https://github.com/KyrieChao/Failure) —— 一个轻量Spring Boot 参数校验与异常处理框架的性能测试项目。

> 💡 **提示**：本项目需配合 [Failure 核心库](https://github.com/KyrieChao/Failure) 使用。

[![JDK](https://img.shields.io/badge/JDK-17%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/KyrieChao/Failure/blob/main/LICENSE)

---

## 🔍 项目简介

本仓库包含针对 [`Failure`](https://github.com/KyrieChao/Failure) 库的**微基准测试（Microbenchmarks）** 和**集成性能对比**，旨在评估其在真实场景下的性能表现。

主要对比内容包括：
- `Failure`（快速失败模式） vs 原生 `@Valid`（Hibernate Validator）
- `Failure`（严格模式，收集全部错误） vs 手动 `if` 校验链
- 使用 `@Validate` 注解（基于 AOP）的运行时开销
- 高并发下的内存分配与 GC 压力

所有基准测试均基于 **[JMH（Java Microbenchmark Harness）](https://openjdk.org/projects/code-tools/jmh/)** 构建，并在典型的 Spring Boot 应用上下文中运行。

---

## 📈 性能测试报告

以下是使用 JMH 在 JDK 17.0.16、3 Forks × 10 Iterations 下得到的性能对比结果：

![Failure Framework 性能测试报告](./images/performance_report.png)

### 图表解读

#### 1️⃣ 左上图：`Failure` vs Hibernate Validator 延迟对比
- **验证通过（Valid）**：
    - `Fail-Fast`: 230 ns/op
    - `Fail-Strict`: 234 ns/op
    - Hibernate Validator: **2584 ns/op** → 比 `Failure` 慢 **11.2x**
- **验证失败（Invalid）**：
    - `Fail-Fast`: 303 ns/op
    - `Fail-Strict`: 646 ns/op
    - Hibernate Validator: **1593 ns/op**

✅ 结论：`Failure` 在失败场景下依然保持极低延迟，**性能显著优于 Hibernate Validator**。

#### 2️⃣ 右上图：对数刻度视角
- 使用对数坐标更清晰展示性能差距。
- `Failure` 的延迟几乎接近底层方法调用成本，而 Hibernate Validator 开销巨大。

#### 3️⃣ 左下图：`TypedValidator` vs 传统 `if-throw`
- 字符串校验（Valid）：`TypedValidator` 比 `if-throw` 快 **0.2x**（即慢 5 倍）
- 字符串校验（Invalid）：`TypedValidator` 快 **12.7x**
- 数值校验（Valid）：`TypedValidator` 比 `if-throw` 快 **0.1x**（慢 10 倍）
- 数值校验（Invalid）：`TypedValidator` 快 **13.2x**

⚠️ 注意：`if-throw` 在成功路径上更快，但 `Failure` 在失败路径（常见于异常输入）中性能碾压。

#### 4️⃣ 右下图：综合性能提升汇总
- `Fail-Fast` vs Hibernate: **8.2x** 提升
- `Fail-Strict` vs Hibernate: **6.7x** 提升
- `TypedValidator` (Invalid) vs Traditional: **12.9x** 提升

> 💡 **核心结论**：  
> 在**用户输入非法（Invalid）的典型场景下**，`Failure` 框架比传统方式快 **6~13 倍**，且在合法输入下也基本无额外开销。

---

## 🚀 快速开始

### 前置要求
- JDK 17 或更高版本
- Maven 3.8+

## ▶️ 如何运行基准测试？

完整的测试运行命令、脚本使用方法和故障排查指南，请参阅：[TEST_RUNNER.md](./TEST_RUNNER.md)
```
示例输出（片段）：
TypedValidator 字符串验证（有效数据）: 61.7228 ns/op
传统 if-throw 字符串验证（无效数据）: 428.9274 ns/op
```