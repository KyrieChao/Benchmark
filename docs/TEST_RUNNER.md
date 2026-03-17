# 测试运行指南

## 🌐 语言 / Language
- [English](TEST_RUNNER_EN.md)
- [中文](./TEST_RUNNER.md)

本指南提供了 `com.chao.benchmark` 包下所有测试类的运行命令（`src/main/java/com/chao/benchmark/`）。

## 编译项目

在运行任何测试之前，确保项目已编译：

```bash
mvn clean compile
```

```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency 
```

## 测试类运行命令

### 手动测试类

| 测试类 | 运行命令 | 说明 |
|--------|---------|------|
| `NoSpringChainBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.NoSpringChainBenchmark` | 无 Spring 依赖的验证链性能测试 |
| `NoSpringTypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.NoSpringTypedValidatorBenchmark` | 无 Spring 依赖的 TypedValidator 性能测试 |
| `ManualTypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ManualTypedValidatorBenchmark` | 手动测试 TypedValidator 性能（验证修复效果） |
| `AopValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.AopValidationBenchmark` | AOP 验证性能测试 |
| `BatchValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.BatchValidationBenchmark` | 批量验证性能测试 |
| `ChainValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ChainValidationBenchmark` | 验证链性能测试 |
| `ComplexObjectValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ComplexObjectValidationBenchmark` | 复杂对象验证性能测试 |
| `ConcurrentValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ConcurrentValidationBenchmark` | 并发验证性能测试 |
| `CoreValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.CoreValidationBenchmark` | 核心验证链性能测试 |
| `I18nValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.I18nValidationBenchmark` | 国际化验证性能测试 |
| `ReflectionCacheBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ReflectionCacheBenchmark` | 反射缓存性能测试 |
| `SimpleValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.SimpleValidationBenchmark` | 简单验证性能测试 |
| `SpringIntegrationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.SpringIntegrationBenchmark` | Spring 集成性能测试 |
| `ValidationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ValidationBenchmark` | 核心验证性能测试 |
| `ValidationChecksBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ValidationChecksBenchmark` | 验证检查性能测试 |
| `ValidationRulesCombinationBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.ValidationRulesCombinationBenchmark` | 验证规则组合性能测试 |
| `TypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.TypedValidatorBenchmark` | TypedValidator 性能测试 |
| `HibernateValidatorComparisonBenchmark` | `java -cp "target\classes;target\dependency\*" com.chao.benchmark.HibernateValidatorComparisonBenchmark` | Hibernate Validator 对比测试 |


或者<br/>
### 使用 [run_benchmarks](../run_benchmarks.py) 脚本运行
![img.png](images/img.png)
![img.png](images/img2.png)
### 脚本分析[analyze](../analyze.py) 
![img.png](images/img3.png)
![img.png](images/img4.png)

**run_benchmarks.py 参数**

| 参数             | 简写   | 说明           | 默认值  |
| -------------- | ---- | ------------ | ---- |
| `--output-dir` | `-o` | 指定测试结果文件保存目录 | 当前目录 |
| `--manual-only` | - | 仅运行手动测试（main 入口） | 关闭 |
| `--jmh-only` | - | 仅运行 JMH（需要存在 JMH 基准或指定 include） | 关闭 |
| `--manual-tests` | - | 指定要运行的手动测试类（逗号分隔） | 全部 |
| `--jmh-includes` | - | 传给 org.openjdk.jmh.Main 的 include 列表（逗号分隔） | 关闭 |
| `--jmh-golden` | - | 运行黄金基准集（`com.chao.benchmark.jmh.*`） | 关闭 |
| `--jmh-args` | - | 追加 JMH 参数（例如 `-wi 5 -i 10 -f 3 -prof gc`） | 关闭 |


**示例：**

```bash
# 默认输出到当前目录
python run_benchmarks.py

# 指定输出目录（自动创建）
python run_benchmarks.py -o ./benchmark-results
python run_benchmarks.py --output-dir D:/Work/failure-benchmark/results

# 仅跑黄金基准集（JMH），并把原始 JSON 写入输出目录
python run_benchmarks.py -o ./benchmark-results --jmh-only --jmh-golden

# 快速校验（缩短 JMH 迭代，仅用于验证链路）
python run_benchmarks.py -o ./benchmark-results --jmh-only --jmh-golden --jmh-args "-f 1 -wi 1 -i 1 -r 200ms -w 200ms"
```

### JMH 参数速查（写在 --jmh-args 里）

| 参数 | 含义 | 影响 |
|------|------|------|
| `-f N` | Fork 次数（独立 JVM 进程数） | 更“干净”的重复实验，可信度更高，但更慢 |
| `-wi N` | Warmup 迭代次数 | 预热更充分，减少 JIT/冷启动影响 |
| `-w T` | 每次 Warmup 的时长 | 单次预热窗口更长，噪声更小 |
| `-i N` | Measurement 迭代次数 | 统计更稳定，可用于对外比较 |
| `-r T` | 每次 Measurement 的时长 | 单次测量窗口更长，噪声更小 |

### 推荐跑法（按用途分档）

```bash
# 1) 超快链路校验（几十秒~几分钟）：只为确认 JMH/JSON/脚本链路 OK
python run_benchmarks.py -o ./benchmark-results --jmh-only --jmh-includes com.chao.benchmark.jmh.ChecksGoldenBenchmark.* --jmh-args "-f 1 -wi 1 -i 1 -r 200ms -w 200ms"

# 2) 快速趋势回归（分钟级）：跑核心对比，适合本地/CI
python run_benchmarks.py -o ./benchmark-results --jmh-only --jmh-includes com.chao.benchmark.jmh.ValidationGoldenBenchmark.* --jmh-args "-f 1 -wi 3 -i 5 -r 1s -w 1s"

# 3) 对外引用（更严格，更慢）：跑核心对比 + 多 fork
python run_benchmarks.py -o ./benchmark-results --jmh-only --jmh-includes com.chao.benchmark.jmh.ValidationGoldenBenchmark.* --jmh-args "-f 3 -wi 5 -i 10 -r 1s -w 1s"
```

**analyze.py 参数**

| 参数             | 简写   | 说明                         | 默认值        |
| -------------- | ---- | -------------------------- | ---------- |
| `input_dir`    | 位置参数 | 包含 `benchmark-*.txt` 文件的目录 | `.` (当前目录) |
| `--output-dir` | `-o` | 分析报告保存目录                   | 当前目录       |

**示例：**
```bash
# 分析当前目录的测试文件，报告输出到当前目录
python analyze.py

# 分析指定目录，报告输出到当前目录
python analyze.py ./benchmark-results

# 指定输入和输出目录（完全分离）
python analyze.py ./benchmark-results -o ./analysis-reports
python analyze.py D:/Work/failure-benchmark/results --output-dir D:/Work/failure-benchmark/analysis

# 使用绝对路径
python analyze.py "D:\Work\failure-benchmark\results" -o "D:\Work\failure-benchmark\analysis"
```


### 组合使用流程（推荐）
```bash
# 1. 先创建专门的目录
mkdir benchmark-results
mkdir analysis-reports

# 2. 运行测试，结果存到 benchmark-results
python run_benchmarks.py -o benchmark-results

# 3. 分析结果，报告存到 analysis-reports  
python analyze.py benchmark-results -o analysis-reports
```

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
   - 本仓库默认脚本不会强制运行 JMH（如果没有 JMH 基准，会自动跳过）
   - 如需运行 JMH，可用 `--jmh-includes` 传入 include（例如某个 `com.chao.benchmark.jmh.*`）

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


## 总结

本指南提供了 com.chao.benchmark 包下所有测试类的运行命令，帮助您快速执行性能测试并验证效果。如果遇到任何问题，请参考故障排除部分或联系项目维护者。
