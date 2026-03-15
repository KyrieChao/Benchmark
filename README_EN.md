# 📊 Failure Performance Benchmark

> A performance testing project for [Failure](https://github.com/KyrieChao/Failure) — a lightweight Spring Boot parameter validation and exception handling framework.

> 💡 **Note**: This project requires the [Failure core library](https://github.com/KyrieChao/Failure) to run.

[![JDK](https://img.shields.io/badge/JDK-17%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/KyrieChao/Failure/blob/main/LICENSE)

---

## 🌐 Language / 语言
- [English](./README_EN)
- [中文](./README)

---

## 🔍 Project Overview

This repository contains **microbenchmarks** and **integration performance comparisons** for the [`Failure`](https://github.com/KyrieChao/Failure) library, designed to evaluate its performance in real-world scenarios.

Key comparisons include:
- `Failure` (Fail-Fast mode) vs native `@Valid` (Hibernate Validator)
- `Failure` (Fail-Strict mode, collect all errors) vs manual `if` validation chains
- Runtime overhead of using `@Validate` annotation (AOP-based)
- Memory allocation and GC pressure under high concurrency

All benchmarks are built using **[JMH (Java Microbenchmark Harness)](https://openjdk.org/projects/code-tools/jmh/)** and run in a typical Spring Boot application context.

---

## 📈 Performance Test Report

Below are the performance comparison results using JMH with JDK 17.0.16, 3 Forks × 10 Iterations:

![Failure Framework Performance Test Report](./images/performance_report.png)

### Report Interpretation

#### 1️⃣ Top Left: `Failure` vs Hibernate Validator Latency Comparison
- **Valid Input**:
    - `Fail-Fast`: 230 ns/op
    - `Fail-Strict`: 234 ns/op
    - Hibernate Validator: **2584 ns/op** → **11.2x** slower than `Failure`
- **Invalid Input**:
    - `Fail-Fast`: 303 ns/op
    - `Fail-Strict`: 646 ns/op
    - Hibernate Validator: **1593 ns/op**

✅ Conclusion: `Failure` maintains extremely low latency even in failure scenarios, **significantly outperforming Hibernate Validator**.

#### 2️⃣ Top Right: Logarithmic Scale View
- Logarithmic scale clearly shows the performance gap.
- `Failure` latency is almost close to underlying method call costs, while Hibernate Validator has significant overhead.

#### 3️⃣ Bottom Left: `TypedValidator` vs Traditional `if-throw`
- String validation (Valid): `TypedValidator` is **0.2x** faster than `if-throw` (5x slower)
- String validation (Invalid): `TypedValidator` is **12.7x** faster
- Numeric validation (Valid): `TypedValidator` is **0.1x** faster than `if-throw` (10x slower)
- Numeric validation (Invalid): `TypedValidator` is **13.2x** faster

⚠️ Note: `if-throw` is faster on success paths, but `Failure`碾压 (dominates) in failure paths (common with invalid user input).

#### 4️⃣ Bottom Right: Comprehensive Performance Improvement Summary
- `Fail-Fast` vs Hibernate: **8.2x** improvement
- `Fail-Strict` vs Hibernate: **6.7x** improvement
- `TypedValidator` (Invalid) vs Traditional: **12.9x** improvement

> 💡 **Core Conclusion**:
> In **typical scenarios with invalid user input**, the `Failure` framework is **6~13x faster** than traditional approaches, with minimal overhead for valid inputs.

---

## 🚀 Quick Start

### Prerequisites
- JDK 17 or higher
- Maven 3.8+

## ▶️ How to Run Benchmarks?

For complete test running commands, script usage, and troubleshooting guides, please refer to: [TEST_RUNNER.md](./TEST_RUNNER_EN)
```
Sample output (snippet):
TypedValidator string validation (valid data): 61.7228 ns/op
Traditional if-throw string validation (invalid data): 428.9274 ns/op
```