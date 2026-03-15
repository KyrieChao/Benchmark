# 测试运行指南

本指南提供了 `demo` 目录下所有测试类的运行命令。

## 编译项目

在运行任何测试之前，确保项目已编译：

```bash
mvn clean compile
```
mvn clean compile dependency:copy-dependencies
```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency 
```

## 测试类运行命令

### 1. 手动测试类（不依赖 JMH）

| 测试类 | 运行命令 | 说明 |
|--------|---------|------|
| `NoSpringChainBenchmark` | `java -cp "target\classes;target\dependency\*" demo.NoSpringChainBenchmark` | 无 Spring 依赖的验证链性能测试 |
| `NoSpringTypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" demo.NoSpringTypedValidatorBenchmark` | 无 Spring 依赖的 TypedValidator 性能测试 |
| `ManualTypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ManualTypedValidatorBenchmark` | 手动测试 TypedValidator 性能（验证修复效果） |
| `AopValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.AopValidationBenchmark` | AOP 验证性能测试 |
| `BatchValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.BatchValidationBenchmark` | 批量验证性能测试 |
| `ChainValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ChainValidationBenchmark` | 验证链性能测试 |
| `ComplexObjectValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ComplexObjectValidationBenchmark` | 复杂对象验证性能测试 |
| `ConcurrentValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ConcurrentValidationBenchmark` | 并发验证性能测试 |
| `CoreValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.CoreValidationBenchmark` | 核心验证链性能测试 |
| `I18nValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.I18nValidationBenchmark` | 国际化验证性能测试 |
| `ReflectionCacheBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ReflectionCacheBenchmark` | 反射缓存性能测试 |
| `SimpleValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.SimpleValidationBenchmark` | 简单验证性能测试 |
| `SpringIntegrationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.SpringIntegrationBenchmark` | Spring 集成性能测试 |
| `ValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ValidationBenchmark` | 核心验证性能测试 |
| `ValidationChecksBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ValidationChecksBenchmark` | 验证检查性能测试 |
| `ValidationRulesCombinationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ValidationRulesCombinationBenchmark` | 验证规则组合性能测试 |

### 2. JMH 测试类（依赖 JMH 框架）

| 测试类 | 运行命令 | 说明 |
|--------|---------|------|
| `TypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" org.openjdk.jmh.Main demo.TypedValidatorBenchmark` | TypedValidator 性能测试（使用 JMH） |
| `HibernateValidatorComparisonBenchmark` | `java -cp "target\classes;target\dependency\*" org.openjdk.jmh.Main demo.HibernateValidatorComparisonBenchmark` | Hibernate Validator 对比测试（使用 JMH） |

## 运行注意事项

1. **依赖问题**：
   - 部分测试类可能依赖 Spring 相关库，运行时可能会遇到 `ClassNotFoundException`
   - 无 Spring 依赖的测试类（如 `NoSpring*` 系列）应该可以正常运行

2. **性能测试环境**：
   - 运行性能测试时，建议关闭其他占用系统资源的程序
   - 多次运行测试以获得更稳定的结果
   - 不同环境下的测试结果可能会有差异

3. **测试结果解读**：
   - `ns/op`：每操作的纳秒数，数值越小表示性能越好
   - `有效数据`：验证通过的场景
   - `无效数据`：验证失败的场景
   - `Fail-Fast`：快速失败模式，一旦发现错误就立即停止
   - `Fail-Strict`：严格模式，收集所有错误后再停止

4. **JMH 测试特殊说明**：
   - JMH 测试会自动进行预热（Warmup）和测量（Measurement）
   - 测试结果会包含统计信息，如平均值、标准差等
   - 运行时间可能较长，耐心等待测试完成

## 故障排除

如果遇到 `ClassNotFoundException` 或其他依赖问题，可以尝试：

1. 确保项目已正确编译：`mvn clean compile`
2. 检查 Maven 依赖是否完整：`mvn dependency:tree`
3. 对于 Spring 相关的测试，确保 Spring 依赖已正确添加
4. 对于 JMH 测试，确保 JMH 依赖已正确添加

## 示例输出

### 手动测试类输出示例：

```
开始无 Spring 依赖的 TypedValidator 性能测试...
测试次数: 1000000
=======================================
TypedValidator 字符串验证（有效数据）: 61.7228 ns/op
TypedValidator 数值验证（有效数据）: 38.8777 ns/op
传统 if-throw 字符串验证（有效数据）: 6.9409 ns/op
传统 if-throw 字符串验证（无效数据）: 428.9274 ns/op
传统 if-throw 数值验证（有效数据）: 2.4891 ns/op
传统 if-throw 数值验证（无效数据）: 461.6256 ns/op
=======================================
无 Spring 依赖的 TypedValidator 性能测试完成！
```

### JMH 测试类输出示例：

```
# JMH version: 1.37
# VM version: JDK 17.0.10, Java HotSpot(TM) 64-Bit Server VM, 17.0.10+11-LTS-240
# VM invoker: C:\Program Files\Java\jdk-17.0.10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 1 s each
# Measurement: 10 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: demo.TypedValidatorBenchmark.testTypedValidatorStringValid

# Run progress: 0.00% complete, ETA 00:05:00
# Fork: 1 of 3
# Warmup Iteration   1: 53.370 ns/op
# Warmup Iteration   2: 52.145 ns/op
# Warmup Iteration   3: 51.982 ns/op
# Warmup Iteration   4: 52.018 ns/op
# Warmup Iteration   5: 51.991 ns/op
Iteration   1: 52.003 ns/op
Iteration   2: 51.987 ns/op
Iteration   3: 51.994 ns/op
Iteration   4: 52.001 ns/op
Iteration   5: 51.996 ns/op
Iteration   6: 51.998 ns/op
Iteration   7: 52.000 ns/op
Iteration   8: 51.995 ns/op
Iteration   9: 51.999 ns/op
Iteration  10: 52.002 ns/op

# Run progress: 33.33% complete, ETA 00:03:20
# Fork: 2 of 3
...

Result "demo.TypedValidatorBenchmark.testTypedValidatorStringValid":
  52.000 ±(99.9%) 0.005 ns/op [Average]
  (min, avg, max) = (51.987, 52.000, 52.003), stdev = 0.005
  CI (99.9%): [51.995, 52.005] (assumes normal distribution)
```

## 总结

本指南提供了 demo 目录下所有测试类的运行命令，帮助您快速执行性能测试并验证修复效果。如果遇到任何问题，请参考故障排除部分或联系项目维护者。