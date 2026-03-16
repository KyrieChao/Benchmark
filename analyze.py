import re
import sys
import argparse
import json
from pathlib import Path
from datetime import datetime
from statistics import mean, stdev

def extract_manual_benchmarks(text):
    """提取手动测试的结果（如：Fail-Fast 简单验证（有效数据）: 228.3323 ns/op）"""
    results = {}
    # 匹配中文测试名 + 数字 + ns/op
    pattern = r'([\u4e00-\u9fa5\w\-\s./()（）]+):\s+([\d.E+-]+)\s*ns/op'

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
    # 匹配 Result "xxx": 123.456 ±(99.9%) 12.345 ns/op
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

def extract_jmh_json_files(input_dir: Path):
    return sorted(input_dir.glob("jmh-*.json"))

def extract_jmh_json_benchmarks(json_path: Path):
    results = {}
    try:
        data = json.loads(json_path.read_text(encoding='utf-8', errors='ignore'))
    except Exception:
        return results

    if not isinstance(data, list):
        return results

    for item in data:
        if not isinstance(item, dict):
            continue
        benchmark = item.get("benchmark")
        mode = item.get("mode")
        pm = item.get("primaryMetric") or {}
        score = pm.get("score")
        score_error = pm.get("scoreError")
        score_unit = pm.get("scoreUnit")
        if benchmark is None or score is None:
            continue

        norm_score = float(score)
        norm_unit = score_unit
        if mode == "avgt" and isinstance(score_unit, str):
            if score_unit == "s/op":
                norm_score = norm_score * 1e9
                norm_unit = "ns/op"
            elif score_unit == "ms/op":
                norm_score = norm_score * 1e6
                norm_unit = "ns/op"
            elif score_unit == "us/op":
                norm_score = norm_score * 1e3
                norm_unit = "ns/op"

        sec = item.get("secondaryMetrics") or {}
        alloc = None
        alloc_unit = None
        alloc_metric = sec.get("gc.alloc.rate.norm")
        if isinstance(alloc_metric, dict):
            alloc = alloc_metric.get("score")
            alloc_unit = alloc_metric.get("scoreUnit")

        short_name = str(benchmark).split('.')[-1]
        results.setdefault(short_name, []).append({
            "raw": benchmark,
            "mode": mode,
            "score": norm_score,
            "error": float(score_error) if score_error is not None else None,
            "unit": norm_unit,
            "alloc": float(alloc) if alloc is not None else None,
            "alloc_unit": alloc_unit,
            "file": json_path.name,
        })

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
  python analyze.py ./results -o ./reports     # 指定输入和输出目录
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
    jmh_json_files = extract_jmh_json_files(input_dir)

    if not files and not jmh_json_files:
        print(f"错误: 在 {input_dir.absolute()} 中未找到 benchmark-*.txt 或 jmh-*.json 文件")
        sys.exit(1)

    if files:
        print(f"找到 {len(files)} 个测试文件")
    if jmh_json_files:
        print(f"找到 {len(jmh_json_files)} 个 JMH JSON 文件")

    # 分析所有文件
    all_data = [analyze_file(f) for f in files] if files else []

    manual_aggregate = {}
    for data in all_data:
        for name, values in data['manual'].items():
            manual_aggregate.setdefault(name, []).extend(values)

    jmh_aggregate = {}
    for data in all_data:
        for name, result in data['jmh'].items():
            jmh_aggregate.setdefault(name, []).append(result['score'])

    jmh_json_aggregate = {}
    for jf in jmh_json_files:
        per_file = extract_jmh_json_benchmarks(jf)
        for name, runs in per_file.items():
            jmh_json_aggregate.setdefault(name, []).extend(runs)

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
        if files:
            log(f"**测试轮数**: {len(files)} 轮")
        if jmh_json_files:
            log(f"**JMH JSON**: {len(jmh_json_files)} 份")
        log("")

        if manual_aggregate:
            log("## 手动测试汇总（多轮统计）")
            log("")
            log("| 测试项 | 平均 | 标准差 | 最小~最大 | 样本数 |")
            log("|--------|------|--------|-----------|--------|")
            for name in sorted(manual_aggregate.keys()):
                values = manual_aggregate[name]
                avg = mean(values)
                sd = stdev(values) if len(values) >= 2 else 0.0
                log(f"| {name} | {avg:.2f} | {sd:.2f} | {min(values):.2f} ~ {max(values):.2f} | {len(values)} |")
            log("")

            def get_one(key):
                values = manual_aggregate.get(key)
                return mean(values) if values else None

            log("## 关键性能对比（来自手动测试）")
            log("")

            ff_valid = get_one("Failure Fail-Fast（有效数据）")
            hv_valid = get_one("Hibernate Validator（有效数据）")
            if ff_valid and hv_valid:
                log(f"- Failure(Fail-Fast, 有效) vs Hibernate(有效): **{hv_valid / ff_valid:.1f}x**（{ff_valid:.2f} vs {hv_valid:.2f} ns/op）")

            fs_valid = get_one("Failure Fail-Strict（有效数据）")
            if fs_valid and hv_valid:
                log(f"- Failure(Fail-Strict, 有效) vs Hibernate(有效): **{hv_valid / fs_valid:.1f}x**（{fs_valid:.2f} vs {hv_valid:.2f} ns/op）")

            ff_invalid = get_one("Failure Fail-Fast（无效数据）")
            hv_invalid = get_one("Hibernate Validator（无效数据）")
            if ff_invalid and hv_invalid:
                log(f"- Failure(Fail-Fast, 无效) vs Hibernate(无效): **{hv_invalid / ff_invalid:.1f}x**（{ff_invalid:.2f} vs {hv_invalid:.2f} ns/op）")

            fs_invalid = get_one("Failure Fail-Strict（无效数据）")
            if fs_invalid and hv_invalid:
                log(f"- Failure(Fail-Strict, 无效) vs Hibernate(无效): **{hv_invalid / fs_invalid:.1f}x**（{fs_invalid:.2f} vs {hv_invalid:.2f} ns/op）")

            tv_str_invalid = get_one("TypedValidator 字符串验证（无效数据）")
            ifthrow_str_invalid = get_one("传统 if-throw 字符串验证（无效数据）")
            if tv_str_invalid and ifthrow_str_invalid:
                log(f"- TypedValidator(字符串, 无效) vs if-throw(字符串, 无效): **{ifthrow_str_invalid / tv_str_invalid:.1f}x**（{tv_str_invalid:.2f} vs {ifthrow_str_invalid:.2f} ns/op）")

            tv_num_invalid = get_one("TypedValidator 数值验证（无效数据）")
            ifthrow_num_invalid = get_one("传统 if-throw 数值验证（无效数据）")
            if tv_num_invalid and ifthrow_num_invalid:
                log(f"- TypedValidator(数值, 无效) vs if-throw(数值, 无效): **{ifthrow_num_invalid / tv_num_invalid:.1f}x**（{tv_num_invalid:.2f} vs {ifthrow_num_invalid:.2f} ns/op）")

            log("")
        else:
            log("## 手动测试汇总")
            log("")
            log("- 未找到手动测试数据")
            log("")

        if jmh_json_aggregate:
            log("## JMH 结果（来自 JSON）")
            log("")
            log("| 测试项 | 模式 | 平均 | 最小~最大 | alloc/op(B/op) | 样本数 |")
            log("|--------|------|------|-----------|--------------|--------|")

            for name in sorted(jmh_json_aggregate.keys()):
                runs = jmh_json_aggregate[name]
                scores = [r["score"] for r in runs if r.get("score") is not None]
                allocs = [r["alloc"] for r in runs if r.get("alloc") is not None]
                if not scores:
                    continue
                alloc_text = "-"
                if allocs:
                    alloc_text = f"{mean(allocs):.2f}"
                mode = runs[0].get("mode") or "-"
                unit = runs[0].get("unit") or ""
                log(f"| {name} | {mode} | {mean(scores):.2f} {unit} | {min(scores):.2f} ~ {max(scores):.2f} | {alloc_text} | {len(scores)} |")

            log("")

        if jmh_aggregate and not jmh_json_aggregate:
            log("## JMH 结果（纳秒/操作）")
            log("")
            log("| 测试项 | 平均 | 最小~最大 | 相对性能 |")
            log("|--------|------|-----------|----------|")

            baseline = min(jmh_aggregate.values(), key=lambda x: mean(x))
            baseline_mean = mean(baseline)

            for name in sorted(jmh_aggregate.keys()):
                scores = jmh_aggregate[name]
                avg_score = mean(scores)
                log(f"| {name} | {avg_score:.2f} | {min(scores):.2f} ~ {max(scores):.2f} | {baseline_mean / avg_score:.1f}x |")

            log("")

        # 原始数据附录
        log("## 原始数据文件")
        log("")
        for data in all_data:
            log(f"- `{data['file'].name}` ({data['timestamp']})")
        for jf in jmh_json_files:
            log(f"- `{jf.name}`")

        log("")
        log("---")
        log("*报告由 analyze.py 自动生成*")

    print(f"\n[完成] 分析报告: {report_file}")

if __name__ == "__main__":
    main()
