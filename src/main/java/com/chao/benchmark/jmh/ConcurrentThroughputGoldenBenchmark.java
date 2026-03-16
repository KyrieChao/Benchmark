package com.chao.benchmark.jmh;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@JmhConfig
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ConcurrentThroughputGoldenBenchmark {
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    @State(Scope.Thread)
    public static class Input {
        @Param({"FAIL_FIRST", "FAIL_MIDDLE", "FAIL_LAST"})
        public String failPosition;

        @Param({"1", "10", "50"})
        public int invalidRatePercent;

        private boolean[] invalidFlags;
        private int flagIndex;

        private String validUsername;
        private String invalidUsername;
        private String validEmail;
        private String invalidEmail;
        private int validAge;
        private int invalidAge;

        public String username;
        public String email;
        public int age;

        @Setup(Level.Trial)
        public void setupTrial() {
            validUsername = "testuser";
            invalidUsername = "";
            validEmail = "test@example.com";
            invalidEmail = "invalid-email";
            validAge = 20;
            invalidAge = 15;

            SplittableRandom r = new SplittableRandom(7654321L);
            invalidFlags = new boolean[1024];
            for (int i = 0; i < invalidFlags.length; i++) {
                invalidFlags[i] = r.nextInt(100) < invalidRatePercent;
            }
            flagIndex = 0;
        }

        public void nextInput() {
            boolean invalid = invalidFlags[flagIndex++ & (invalidFlags.length - 1)];
            if (!invalid) {
                username = validUsername;
                email = validEmail;
                age = validAge;
                return;
            }

            switch (failPosition) {
                case "FAIL_FIRST" -> {
                    username = invalidUsername;
                    email = validEmail;
                    age = validAge;
                }
                case "FAIL_MIDDLE" -> {
                    username = validUsername;
                    email = invalidEmail;
                    age = validAge;
                }
                default -> {
                    username = validUsername;
                    email = validEmail;
                    age = invalidAge;
                }
            }
        }
    }

    private static int failureFastIsValid(Input in) {
        in.nextInput();
        Chain chain = Failure.begin();
        chain.notBlank(in.username, USERNAME_REQUIRED)
                .email(in.email, EMAIL_INVALID)
                .greaterOrEqual(in.age, 18, AGE_TOO_YOUNG);
        return chain.isValid() ? 1 : 0;
    }

    @Benchmark
    @Threads(1)
    public int t1_failureFailFast_isValid(Input in) {
        return failureFastIsValid(in);
    }

    @Benchmark
    @Threads(2)
    public int t2_failureFailFast_isValid(Input in) {
        return failureFastIsValid(in);
    }

    @Benchmark
    @Threads(4)
    public int t4_failureFailFast_isValid(Input in) {
        return failureFastIsValid(in);
    }

    @Benchmark
    @Threads(8)
    public int t8_failureFailFast_isValid(Input in) {
        return failureFastIsValid(in);
    }

    @Benchmark
    @Threads(16)
    public int t16_failureFailFast_isValid(Input in) {
        return failureFastIsValid(in);
    }
}
