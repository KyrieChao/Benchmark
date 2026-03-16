package com.chao.benchmark.jmh;

import com.chao.failfast.internal.check.NumberChecks;
import com.chao.failfast.internal.check.StringChecks;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@JmhConfig
@BenchmarkMode(Mode.AverageTime)
public class ChecksGoldenBenchmark {
    @State(Scope.Thread)
    public static class Input {
        @Param({"VALID", "INVALID"})
        public String validity;

        public String s;
        public Integer n;

        @Setup(Level.Iteration)
        public void setupIteration() {
            if ("VALID".equals(validity)) {
                s = "test@example.com";
                n = 20;
            } else {
                s = "";
                n = 15;
            }
        }
    }

    @Benchmark
    public boolean string_notBlank(Input in) {
        return StringChecks.notBlank(in.s);
    }

    @Benchmark
    public boolean string_email(Input in) {
        return StringChecks.email(in.s);
    }

    @Benchmark
    public boolean number_greaterOrEqual(Input in) {
        return NumberChecks.greaterOrEqual(in.n, 18);
    }
}
