# Test Runner Guide

## 🌐 Language / 语言
- [English](./TEST_RUNNER_EN)
- [中文](./TEST_RUNNER)

This guide provides run commands for all test classes in the `demo` directory.

## Compile the Project

Before running any tests, ensure the project is compiled:

```bash
mvn clean compile
```
mvn clean compile dependency:copy-dependencies
```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency 
```

## Test Class Run Commands

### Manual Test Classes

| Test Class | Run Command | Description |
|------------|-------------|-------------|
| `NoSpringChainBenchmark` | `java -cp "target\classes;target\dependency\*" demo.NoSpringChainBenchmark` | Validation chain performance test without Spring dependencies |
| `NoSpringTypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" demo.NoSpringTypedValidatorBenchmark` | TypedValidator performance test without Spring dependencies |
| `ManualTypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ManualTypedValidatorBenchmark` | Manual TypedValidator performance test (to verify fix effectiveness) |
| `AopValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.AopValidationBenchmark` | AOP validation performance test |
| `BatchValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.BatchValidationBenchmark` | Batch validation performance test |
| `ChainValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ChainValidationBenchmark` | Validation chain performance test |
| `ComplexObjectValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ComplexObjectValidationBenchmark` | Complex object validation performance test |
| `ConcurrentValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ConcurrentValidationBenchmark` | Concurrent validation performance test |
| `CoreValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.CoreValidationBenchmark` | Core validation chain performance test |
| `I18nValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.I18nValidationBenchmark` | Internationalization validation performance test |
| `ReflectionCacheBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ReflectionCacheBenchmark` | Reflection cache performance test |
| `SimpleValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.SimpleValidationBenchmark` | Simple validation performance test |
| `SpringIntegrationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.SpringIntegrationBenchmark` | Spring integration performance test |
| `ValidationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ValidationBenchmark` | Core validation performance test |
| `ValidationChecksBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ValidationChecksBenchmark` | Validation checks performance test |
| `ValidationRulesCombinationBenchmark` | `java -cp "target\classes;target\dependency\*" demo.ValidationRulesCombinationBenchmark` | Validation rules combination performance test |
| `TypedValidatorBenchmark` | `java -cp "target\classes;target\dependency\*" org.openjdk.jmh.Main demo.TypedValidatorBenchmark` | TypedValidator performance test (using JMH) |
| `HibernateValidatorComparisonBenchmark` | `java -cp "target\classes;target\dependency\*" org.openjdk.jmh.Main demo.HibernateValidatorComparisonBenchmark` | Hibernate Validator comparison test (using JMH) |


Or<br/>
### Run with [run_benchmarks](./run_benchmarks.py) script
![img.png](images/img.png)
![img.png](images/img2.png)
### Script analysis [analyze](./analyze.py) 
![img.png](images/img3.png)
![img.png](images/img4.png)

**run_benchmarks.py parameters**

| Parameter | Short | Description | Default |
|-----------|-------|-------------|---------|
| `--output-dir` | `-o` | Specify directory to save test result files | Current directory |

**Examples:**

```bash
# Output to current directory by default
python run_benchmarks.py

# Specify output directory (created automatically)
python run_benchmarks.py -o ./benchmark-results
python run_benchmarks.py --output-dir D:/Work/failure-benchmark/results
```

**analyze.py parameters**

| Parameter | Short | Description | Default |
|-----------|-------|-------------|---------|
| `input_dir` | Positional | Directory containing `benchmark-*.txt` files | `.` (current directory) |
| `--output-dir` | `-o` | Analysis report save directory | Current directory |

**Examples:**
```bash
# Analyze test files in current directory, output report to current directory
python analyze.py

# Analyze specified directory, output report to current directory
python analyze.py ./benchmark-results

# Specify input and output directories (completely separated)
python analyze.py ./benchmark-results -o ./analysis-reports
python analyze.py D:/Work/failure-benchmark/results --output-dir D:/Work/failure-benchmark/analysis

# Using absolute paths
python analyze.py "D:\Work\failure-benchmark\results" -o "D:\Work\failure-benchmark\analysis"
```


### Recommended Combined Usage Flow
```bash
# 1. First create dedicated directories
mkdir benchmark-results
mkdir analysis-reports

# 2. Run tests, save results to benchmark-results
python run_benchmarks.py -o benchmark-results

# 3. Analyze results, save reports to analysis-reports  
python analyze.py benchmark-results -o analysis-reports
```

## Running Notes

1. **Dependency Issues**:
   - Some test classes may depend on Spring-related libraries and may encounter `ClassNotFoundException` when running
   - Test classes without Spring dependencies (like the `NoSpring*` series) should run normally

2. **Performance Test Environment**:
   - When running performance tests, it's recommended to close other programs that consume system resources
   - Run tests multiple times to get more stable results
   - Test results may vary in different environments

3. **Test Result Interpretation**:
   - `ns/op`: Nanoseconds per operation, smaller values indicate better performance
   - `Valid data`: Scenarios where validation passes
   - `Invalid data`: Scenarios where validation fails
   - `Fail-Fast`: Fast failure mode, stops immediately once an error is found
   - `Fail-Strict`: Strict mode, collects all errors before stopping

4. **JMH Test Special Notes**:
   - JMH tests automatically perform warmup and measurement
   - Test results will include statistical information such as mean, standard deviation, etc.
   - Running time may be longer, please wait patiently for the test to complete

## Troubleshooting

If you encounter `ClassNotFoundException` or other dependency issues, you can try:

1. Ensure the project is correctly compiled: `mvn clean compile`
2. Check if Maven dependencies are complete: `mvn dependency:tree`
3. For Spring-related tests, ensure Spring dependencies are correctly added
4. For JMH tests, ensure JMH dependencies are correctly added

## Sample Output

### Manual Test Class Output Example:

```
Starting TypedValidator performance test without Spring dependencies...
Test count: 1000000
=======================================
TypedValidator string validation (valid data): 61.7228 ns/op
TypedValidator numeric validation (valid data): 38.8777 ns/op
Traditional if-throw string validation (valid data): 6.9409 ns/op
Traditional if-throw string validation (invalid data): 428.9274 ns/op
Traditional if-throw numeric validation (valid data): 2.4891 ns/op
Traditional if-throw numeric validation (invalid data): 461.6256 ns/op
=======================================
TypedValidator performance test without Spring dependencies completed!
```


## Summary

This guide provides run commands for all test classes in the demo directory, helping you quickly execute performance tests and verify fix effectiveness. If you encounter any issues, please refer to the troubleshooting section or contact the project maintainers.