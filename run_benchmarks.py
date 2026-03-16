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
BASE_PACKAGE = "com.chao.benchmark"
GOLDEN_JMH_INCLUDES = [f"{BASE_PACKAGE}.jmh.*"]

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
JMH_TESTS = []

def get_java_cmd(class_name, is_jmh=False, jmh_includes=None, jmh_args=None):
    """构建 Java 命令，强制 UTF-8 编码"""
    base_cmd = [
        "java",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-cp", CLASSPATH,
    ]

    if is_jmh:
        base_cmd.extend(["org.openjdk.jmh.Main"])
        if jmh_args:
            base_cmd.extend(jmh_args)
        includes = jmh_includes or [f"{BASE_PACKAGE}.{class_name}"]
        base_cmd.extend(includes)
    else:
        base_cmd.append(f"{BASE_PACKAGE}.{class_name}")

    return base_cmd

def run_java(class_name, is_jmh=False, jmh_includes=None, jmh_args=None):
    """运行 Java 测试类"""
    cmd = get_java_cmd(class_name, is_jmh, jmh_includes=jmh_includes, jmh_args=jmh_args)

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
    return output, result.returncode

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
    parser.add_argument('--manual-only', action='store_true', help='仅运行手动测试（main 入口）')
    parser.add_argument('--jmh-only', action='store_true', help='仅运行 JMH（需要存在 JMH 基准）')
    parser.add_argument(
        '--manual-tests',
        type=str,
        default=None,
        help='指定要运行的手动测试类（逗号分隔，类名不含包名）'
    )
    parser.add_argument(
        '--jmh-includes',
        type=str,
        default=None,
        help='传给 org.openjdk.jmh.Main 的 include 列表（逗号分隔，默认用类全名）'
    )
    parser.add_argument('--jmh-golden', action='store_true', help='运行黄金基准集（com.chao.benchmark.jmh.*）')
    parser.add_argument(
        '--jmh-args',
        type=str,
        default=None,
        help='追加传给 org.openjdk.jmh.Main 的参数（例如: -prof gc -wi 5 -i 10 -f 3）'
    )
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

        log(f"\n测试次数(多数 case 内置): {TEST_COUNT:,}\n")

        manual_tests = MANUAL_TESTS
        if args.manual_tests:
            manual_tests = [s.strip() for s in args.manual_tests.split(',') if s.strip()]

        jmh_includes = None
        if args.jmh_includes:
            jmh_includes = [s.strip() for s in args.jmh_includes.split(',') if s.strip()]
        if args.jmh_golden:
            jmh_includes = GOLDEN_JMH_INCLUDES

        jmh_args = None
        if args.jmh_args:
            jmh_args = [s for s in args.jmh_args.split() if s.strip()]

        run_manual = not args.jmh_only
        run_jmh = not args.manual_only

        if run_manual:
            log("=" * 60)
            log("手动测试（main 入口）")
            log("=" * 60)

            for test in manual_tests:
                log(f"\n{'-' * 60}")
                log(f"[{test}] 开始: {datetime.now().strftime('%H:%M:%S')}")

                output, code = run_java(test)
                f.write(output)

                if code != 0 or "错误:" in output or "Exception" in output:
                    log(f"[失败] {test} 运行出错（exit={code}），见上文")
                else:
                    log(f"[成功] {test} 完成")

                log(f"[{test}] 结束: {datetime.now().strftime('%H:%M:%S')}")

        if run_jmh:
            log(f"\n{'=' * 60}")
            log("JMH 测试")
            log(f"{'=' * 60}")

            if not JMH_TESTS and not jmh_includes:
                log("- 未配置 JMH 基准（可用 --jmh-includes 传入 include 模式）")
            else:
                jmh_json = output_path / f"jmh-{timestamp}.json"
                default_jmh_args = ["-rf", "json", "-rff", str(jmh_json), "-prof", "gc"]
                effective_jmh_args = default_jmh_args + (jmh_args or [])

                tests = JMH_TESTS if JMH_TESTS else ["<custom>"]
                for test in tests:
                    log(f"\n{'-' * 60}")
                    log(f"[JMH {test}] 开始: {datetime.now().strftime('%H:%M:%S')}")

                    output, code = run_java(test, is_jmh=True, jmh_includes=jmh_includes, jmh_args=effective_jmh_args)
                    f.write(output)

                    if code != 0 or "错误:" in output or "ClassNotFoundException" in output:
                        log(f"[失败] JMH {test} 运行出错（exit={code}）")
                    else:
                        log(f"[成功] JMH {test} 完成")

                    log(f"[JMH {test}] 结束: {datetime.now().strftime('%H:%M:%S')}")

        log(f"\n测试完成: {datetime.now()}")

    print(f"\n完成！结果保存在: {result_file}")

if __name__ == "__main__":
    main()
