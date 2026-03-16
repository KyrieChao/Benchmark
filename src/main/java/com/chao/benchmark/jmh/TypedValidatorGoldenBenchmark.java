package com.chao.benchmark.jmh;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;
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
public class TypedValidatorGoldenBenchmark {
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    private static final class GoldenTypedValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            register(String.class, (value, context) -> {
                Chain chain = Chain.begin(true);
                chain.notBlank(value, USERNAME_REQUIRED)
                        .lengthMin(value, 3, USERNAME_REQUIRED)
                        .lengthMax(value, 20, USERNAME_REQUIRED);
                if (!chain.isValid()) {
                    context.reportError(USERNAME_REQUIRED);
                }
            });

            register(Integer.class, (value, context) -> {
                Chain chain = Chain.begin(true);
                chain.greaterOrEqual(value, 18, AGE_TOO_YOUNG);
                if (!chain.isValid()) {
                    context.reportError(AGE_TOO_YOUNG);
                }
            });
        }
    }

    @State(Scope.Thread)
    public static class Input {
        @Param({"STRING_VALID", "STRING_INVALID", "INT_VALID", "INT_INVALID", "MISS"})
        public String scenario;

        private GoldenTypedValidator validator;
        private FastValidator.ValidationContext context;
        private Object value;

        @Setup(Level.Trial)
        public void setupTrial() {
            validator = new GoldenTypedValidator();
            context = new FastValidator.ValidationContext(true);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            switch (scenario) {
                case "STRING_VALID" -> value = "testuser";
                case "STRING_INVALID" -> value = "";
                case "INT_VALID" -> value = 20;
                case "INT_INVALID" -> value = 15;
                default -> value = 1L;
            }
        }
    }

    @Benchmark
    public int typedValidator_validate(Input in) {
        in.context.reset();
        in.validator.validate(in.value, in.context);
        return in.context.isValid() ? 1 : 0;
    }
}
