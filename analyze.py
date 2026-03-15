import re
import sys
import argparse
from pathlib import Path
from datetime import datetime
from statistics import mean, stdev

def extract_manual_benchmarks(text):
    """提取手动测试的结果（如：Fail-Fast 简单验证（有效数据）: 228.3323 ns/op）"""
    results = {}
    # 匹配中文测试名 + 数字 + ns/op
    pattern = r'([\u4e00-\u9fa5\w\-()（）]+):\s+([\d.E+-]+)\s*ns/op'

    for line in text.split('\n'):
        match = re.search(pattern, line)
        if match and 'ns/op' in line:
            name = match.group(1).strip()
            try:
                value = float(match.group(2))
                if name not in results:
                    results[name] = []
                results[name].append(value)
            except ValueError:
                pass

    return results

def extract_jmh_benchmarks(text):
    """提取 JMH 的 Result 行"""
    results = {}
    # 匹配 Result "demo.xxx": 123.456 ±(99.9%) 12.345 ns/op
    pattern = r'Result "(.*?)":\s+([\d.]+).*?\u00b1\(99\.9%\)\s+([\d.]+)\s*ns/op'

    for match in re.finditer(pattern, text):
        benchmark = match.group(1)
        score = float(match.group(2))
        error = float(match.group(3))

        # 简化名称
        short_name = benchmark.split('.')[-1]
        results[short_name] = {
            'score': score,
            'error': error,
            'raw': benchmark
        }

    return results

def analyze_file(filepath):
    """分析单个 benchmark 文件"""
    text = Path(filepath).read_text(encoding='utf-8', errors='ignore')

    manual = extract_manual_benchmarks(text)
    jmh = extract_jmh_benchmarks(text)

    return {
        'file': filepath,
        'manual': manual,
        'jmh': jmh,
        'timestamp': extract_timestamp(text)
    }

def extract_timestamp(text):
    """提取文件中的时间信息"""
    # 尝试找 "测试完成: xxx" 或文件生成时间
    match = re.search(r'测试完成:\s*(.+)', text)
    return match.group(1).strip() if match else "未知"

def main():
    # 解析命令行参数
    parser = argparse.ArgumentParser(
        description='Failure Framework 性能测试分析工具',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用示例:
  python analyze.py                    # 分析当前目录，报告输出到当前目录
  python analyze.py ./results          # 分析 ./results 目录
  python analyze.py -i ./results -o ./reports  # 指定输入和输出目录
        """
    )
    parser.add_argument(
        'input_dir',
        nargs='?',
        default='.',
        help='输入目录（包含 benchmark-*.txt 文件，默认为当前目录）'
    )
    parser.add_argument(
        '-o', '--output-dir',
        type=str,
        default=None,
        help='报告输出目录（默认为当前目录）'
    )

    args = parser.parse_args()

    # 处理输入目录
    input_dir = Path(args.input_dir)
    if not input_dir.exists():
        print(f"错误: 输入目录不存在: {input_dir.absolute()}")
        sys.exit(1)

    # 处理输出目录
    if args.output_dir:
        output_dir = Path(args.output_dir).resolve()
        output_dir.mkdir(parents=True, exist_ok=True)
        print(f"[配置] 报告将保存到: {output_dir}")
    else:
        output_dir = Path.cwd()
        print(f"[配置] 报告将保存到当前目录: {output_dir}")

    # 查找所有 benchmark 文件
    files = sorted(input_dir.glob("benchmark-*.txt"))

    if not files:
        print(f"错误: 在 {input_dir.absolute()} 中未找到 benchmark-*.txt 文件")
        sys.exit(1)

    print(f"找到 {len(files)} 个测试文件")

    # 分析所有文件
    all_data = [analyze_file(f) for f in files]

    # 生成报告文件路径
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    report_file = output_dir / f"analysis-report-{timestamp}.md"

    with open(report_file, 'w', encoding='utf-8') as f:
        def log(line=""):
            print(line)
            f.write(line + '\n')

        log("# Failure Framework 性能测试分析报告")
        log(f"**生成时间**: {datetime.now()}")
        log(f"**数据来源**: {input_dir.absolute()}")
        log(f"**测试轮数**: {len(files)} 轮")
        log("")

        # 汇总 JMH 数据（多轮平均）
        log("## JMH 严格测试结果（纳秒/操作）")
        log("")
        log("| 测试项 | 平均 | 误差范围 | 相对性能 |")
        log("|--------|------|----------|----------|")

        # 收集所有 JMH 结果用于计算平均
        jmh_aggregate = {}
        for data in all_data:
            for name, result in data['jmh'].items():
                if name not in jmh_aggregate:
                    jmh_aggregate[name] = []
                jmh_aggregate[name].append(result['score'])

        if jmh_aggregate:
            # 计算并排序
            for name in sorted(jmh_aggregate.keys()):
                scores = jmh_aggregate[name]
                avg_score = mean(scores)
                min_score = min(scores)
                max_score = max(scores)

                # 相对性能（以最快为基准）
                baseline = min(jmh_aggregate.values(), key=lambda x: mean(x))
                relative = mean(baseline) / avg_score

                log(f"| {name} | {avg_score:.2f} | {min_score:.2f} ~ {max_score:.2f} | {relative:.1f}x |")
        else:
            log("| 无数据 | - | - | - |")

        log("")

        # 关键对比
        log("## 关键性能对比")
        log("")

        # Failure vs Hibernate
        failure_fast = jmh_aggregate.get('testFailFastValid', [0])
        hibernate = jmh_aggregate.get('testHibernateValidatorValid', [0])

        if failure_fast[0] and hibernate[0]:
            speedup = mean(hibernate) / mean(failure_fast)
            log(f"- **Failure (FailFast)** vs **Hibernate Validator**: 快 **{speedup:.1f} 倍**")
            log(f"  - Failure: {mean(failure_fast):.2f} ns/op")
            log(f"  - Hibernate: {mean(hibernate):.2f} ns/op")
        else:
            log("- 未找到对比数据（需要运行 JMH 测试）")

        log("")

        # TypedValidator 性能
        typed_str = jmh_aggregate.get('testTypedValidatorStringValid', [0])
        typed_num = jmh_aggregate.get('testTypedValidatorNumberValid', [0])

        if typed_str[0]:
            log(f"- **TypedValidator 字符串验证**: {mean(typed_str):.2f} ns/op（极快）")
        if typed_num[0]:
            log(f"- **TypedValidator 数值验证**: {mean(typed_num):.2f} ns/op（极快）")

        log("")

        # 手动测试汇总（取最新一轮或平均）
        log("## 手动测试概览（最新一轮）")
        log("")

        latest = all_data[-1]['manual']

        if latest:
            # 分类展示
            categories = {
                '验证链性能': ['Fail-Fast', 'Fail-Strict', 'if-throw'],
                'TypedValidator': ['TypedValidator'],
                '并发性能': ['线程'],
                '其他': []
            }

            for cat, keywords in categories.items():
                log(f"### {cat}")
                log("")
                found = False
                for name, values in sorted(latest.items()):
                    if any(k in name for k in keywords) or (not keywords and not any(k in name for k in sum(categories.values(), []))):
                        if values:
                            avg = mean(values)
                            log(f"- {name}: **{avg:.2f} ns/op**")
                            found = True
                if not found:
                    log("- 无数据")
                log("")
        else:
            log("未找到手动测试数据")
            log("")

        # 原始数据附录
        log("## 原始数据文件")
        log("")
        for data in all_data:
            log(f"- `{data['file'].name}` ({data['timestamp']})")

        log("")
        log("---")
        log("*报告由 analyze.py 自动生成*")

    print(f"\n[完成] 分析报告: {report_file}")

if __name__ == "__main__":
    main()