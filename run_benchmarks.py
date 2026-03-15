import subprocess
import sys
import os
import argparse
from pathlib import Path
from datetime import datetime

def get_classpath():
    """生成平台特定的 Classpath"""
    if sys.platform == 'win32':
        # Windows: target\classes;target\dependency\*
        return "target\\classes;target\\dependency\\*"
    else:
        # Linux/Mac: target/classes:target/dependency/*
        return "target/classes:target/dependency/*"

# 配置
TEST_COUNT = 1_000_000
CLASSPATH = get_classpath()

# 手动测试类列表
MANUAL_TESTS = [
    "NoSpringChainBenchmark",
    "NoSpringTypedValidatorBenchmark",
    "ManualTypedValidatorBenchmark",
    "AopValidationBenchmark",
    "BatchValidationBenchmark",
    "ChainValidationBenchmark",
    "ComplexObjectValidationBenchmark",
    "ConcurrentValidationBenchmark",
    "CoreValidationBenchmark",
    "I18nValidationBenchmark",
    "ReflectionCacheBenchmark",
    "SimpleValidationBenchmark",
    "SpringIntegrationBenchmark",
    "ValidationChecksBenchmark",
    "ValidationRulesCombinationBenchmark",
]

# JMH 测试类列表
JMH_TESTS = [
    "TypedValidatorBenchmark",
    "HibernateValidatorComparisonBenchmark",
]

def get_java_cmd(class_name, is_jmh=False):
    """构建 Java 命令，强制 UTF-8 编码"""
    base_cmd = [
        "java",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-cp", CLASSPATH,
    ]

    if is_jmh:
        base_cmd.extend(["org.openjdk.jmh.Main", f"demo.{class_name}"])
    else:
        base_cmd.append(f"demo.{class_name}")

    return base_cmd

def run_java(class_name, is_jmh=False):
    """运行 Java 测试类"""
    cmd = get_java_cmd(class_name, is_jmh)

    print(f"运行: {class_name}{' (JMH)' if is_jmh else ''}")
    print(f"  命令: java -cp {CLASSPATH} ...")

    env = os.environ.copy()
    env["JAVA_TOOL_OPTIONS"] = "-Dfile.encoding=UTF-8"

    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        encoding='utf-8',
        errors='replace',
        env=env
    )

    output = (result.stdout or "") + (result.stderr or "")
    return output

def check_environment():
    """检查环境"""
    errors = []

    if not Path("target/classes").exists():
        errors.append("未找到 target/classes 目录，请先执行: mvn compile")

    if not Path("target/dependency").exists():
        errors.append("未找到 target/dependency 目录，请先执行: mvn dependency:copy-dependencies")

    if errors:
        print("[环境检查失败]")
        for e in errors:
            print(f"  - {e}")
        sys.exit(1)

def check_windows_encoding():
    """检查 Windows 编码"""
    if sys.platform == 'win32':
        try:
            chcp_result = subprocess.run(["cmd", "/c", "chcp"], capture_output=True, text=True)
            print(f"[系统] 代码页: {chcp_result.stdout.strip()}")
        except Exception:
            pass

def main():
    parser = argparse.ArgumentParser(description='Failure Framework 性能测试')
    parser.add_argument('-o', '--output-dir', type=str, default=None, help='指定输出目录')
    args = parser.parse_args()

    # 环境检查
    check_environment()
    check_windows_encoding()

    # 处理输出目录
    if args.output_dir:
        output_path = Path(args.output_dir).resolve()
        output_path.mkdir(parents=True, exist_ok=True)
    else:
        output_path = Path.cwd()

    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    result_file = output_path / f"benchmark-{timestamp}.txt"

    print("=" * 60)
    print("Failure Framework 性能测试")
    print(f"Classpath: {CLASSPATH}")
    print(f"结果文件: {result_file}")
    print("=" * 60)

    with open(result_file, 'w', encoding='utf-8') as f:
        def log(line):
            print(line)
            f.write(line + '\n')
            f.flush()

        log("测试环境信息")
        log(f"Python: {sys.version}")
        log(f"Classpath: {CLASSPATH}")
        log("JDK版本:")

        # Java 版本
        result = subprocess.run(["java", "-version"], capture_output=True, text=True)
        log(result.stderr or result.stdout)

        log(f"\n测试次数: {TEST_COUNT:,}\n")

        # 手动测试
        log("=" * 60)
        log("手动测试")
        log("=" * 60)

        for test in MANUAL_TESTS:
            log(f"\n{'-' * 60}")
            log(f"[{test}] 开始: {datetime.now().strftime('%H:%M:%S')}")

            output = run_java(test)
            f.write(output)

            # 检查是否真的有错误
            if "错误:" in output or "Exception" in output:
                log(f"[失败] {test} 运行出错，见上文")
            else:
                log(f"[成功] {test} 完成")

            log(f"[{test}] 结束: {datetime.now().strftime('%H:%M:%S')}")

        # JMH 测试
        log(f"\n{'=' * 60}")
        log("JMH测试（每个约6分钟）")
        log(f"{'=' * 60}")

        for test in JMH_TESTS:
            log(f"\n{'-' * 60}")
            log(f"[JMH {test}] 开始: {datetime.now().strftime('%H:%M:%S')}")
            log("[说明] 3 Forks x (5 Warmup + 10 Measurement)")

            output = run_java(test, is_jmh=True)
            f.write(output)

            if "错误:" in output or "ClassNotFoundException" in output:
                log(f"[失败] JMH {test} 运行出错")
            else:
                log(f"[成功] JMH {test} 完成")

            log(f"[JMH {test}] 结束: {datetime.now().strftime('%H:%M:%S')}")

        log(f"\n测试完成: {datetime.now()}")

    print(f"\n完成！结果保存在: {result_file}")

if __name__ == "__main__":
    main()