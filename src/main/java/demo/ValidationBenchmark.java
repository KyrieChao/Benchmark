package demo;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 性能测试类 - 测试验证框架的性能表现
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
@State(Scope.Thread)
public class ValidationBenchmark {

    // 测试数据
    private String validUsername;
    private String invalidUsername;
    private String validEmail;
    private String invalidEmail;
    private int validAge;
    private int invalidAge;

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    @Setup
    public void setup() {
        // 准备测试数据
        validUsername = "testuser";
        invalidUsername = "";
        validEmail = "test@example.com";
        invalidEmail = "invalid-email";
        validAge = 20;
        invalidAge = 15;
    }

    // 测试 1: Fail-Fast 模式 - 简单验证（有效数据）
    @Benchmark
    public void testFailFastSimpleValid(Blackhole blackhole) {
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 2: Fail-Fast 模式 - 简单验证（无效数据）
    @Benchmark
    public void testFailFastSimpleInvalid(Blackhole blackhole) {
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 3: Fail-Strict 模式 - 简单验证（有效数据）
    @Benchmark
    public void testFailStrictSimpleValid(Blackhole blackhole) {
        try {
            Failure.strict()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 4: Fail-Strict 模式 - 简单验证（无效数据）
    @Benchmark
    public void testFailStrictSimpleInvalid(Blackhole blackhole) {
        try {
            Failure.strict()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 5: 传统 if-throw 方式 - 简单验证（有效数据）
    @Benchmark
    public void testTraditionalIfThrowValid(Blackhole blackhole) {
        try {
            if (validUsername == null || validUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (!validEmail.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                throw new RuntimeException("Email is invalid");
            }
            if (validAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 6: 传统 if-throw 方式 - 简单验证（无效数据）
    @Benchmark
    public void testTraditionalIfThrowInvalid(Blackhole blackhole) {
        try {
            if (invalidUsername == null || invalidUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (!invalidEmail.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                throw new RuntimeException("Email is invalid");
            }
            if (invalidAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 7: Fail-Fast 模式 - 复杂验证（有效数据）
    @Benchmark
    public void testFailFastComplexValid(Blackhole blackhole) {
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .lengthMin(validUsername, 3, USERNAME_REQUIRED)
                    .lengthMax(validUsername, 20, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .lessOrEqual(validAge, 100, AGE_TOO_YOUNG)
                    .fail();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 8: Fail-Fast 模式 - 复杂验证（无效数据）
    @Benchmark
    public void testFailFastComplexInvalid(Blackhole blackhole) {
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .lengthMin(invalidUsername, 3, USERNAME_REQUIRED)
                    .lengthMax(invalidUsername, 20, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .lessOrEqual(invalidAge, 100, AGE_TOO_YOUNG)
                    .fail();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ValidationBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
