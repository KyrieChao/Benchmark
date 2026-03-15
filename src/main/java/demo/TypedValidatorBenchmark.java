package demo;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * TypedValidator 性能测试类 - 测试类型化验证器的性能表现
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
public class TypedValidatorBenchmark {

    // 测试数据
    private String validUsername;
    private String invalidUsername;
    private int validAge;
    private int invalidAge;

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    // 自定义TypedValidator实现
    private static class CustomTypedValidator extends TypedValidator {
        @Override
        protected void registerValidators() {
            // 注册字符串验证器
            register(String.class, (value, context) -> {
                Chain chain = Chain.begin(true);
                chain.notBlank(value, USERNAME_REQUIRED)
                     .lengthMin(value, 3, USERNAME_REQUIRED)
                     .lengthMax(value, 20, USERNAME_REQUIRED);
                if (!chain.isValid()) {
                    context.reportError(USERNAME_REQUIRED);
                }
            });

            // 注册整数验证器
            register(Integer.class, (value, context) -> {
                Chain chain = Chain.begin(true);
                chain.greaterOrEqual(value, 18, AGE_TOO_YOUNG);
                if (!chain.isValid()) {
                    context.reportError(AGE_TOO_YOUNG);
                }
            });
        }
    }

    private CustomTypedValidator typedValidator;
    private FastValidator.ValidationContext context;

    @Setup
    public void setup() {
        // 准备测试数据
        validUsername = "testuser";
        invalidUsername = "";
        validAge = 20;
        invalidAge = 15;

        // 初始化验证器（只在每个线程初始化一次）
        typedValidator = new CustomTypedValidator();
        // 初始化验证上下文（只在每个线程初始化一次）
        context = new FastValidator.ValidationContext(true);
    }

    // 测试 1: TypedValidator - 字符串验证（有效数据）
    @Benchmark
    public void testTypedValidatorStringValid(Blackhole blackhole) {
        try {
            // 重置上下文，复用对象
            context.reset();
            typedValidator.validate(validUsername, context);
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 2: TypedValidator - 字符串验证（无效数据）
    @Benchmark
    public void testTypedValidatorStringInvalid(Blackhole blackhole) {
        try {
            // 重置上下文，复用对象
            context.reset();
            typedValidator.validate(invalidUsername, context);
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 3: TypedValidator - 数值验证（有效数据）
    @Benchmark
    public void testTypedValidatorNumberValid(Blackhole blackhole) {
        try {
            // 重置上下文，复用对象
            context.reset();
            typedValidator.validate(validAge, context);
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 4: TypedValidator - 数值验证（无效数据）
    @Benchmark
    public void testTypedValidatorNumberInvalid(Blackhole blackhole) {
        try {
            // 重置上下文，复用对象
            context.reset();
            typedValidator.validate(invalidAge, context);
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 5: 传统 if-throw 方式 - 字符串验证（有效数据）
    @Benchmark
    public void testTraditionalStringValid(Blackhole blackhole) {
        try {
            if (validUsername == null || validUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (validUsername.length() < 3 || validUsername.length() > 20) {
                throw new RuntimeException("Username is required");
            }
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 6: 传统 if-throw 方式 - 字符串验证（无效数据）
    @Benchmark
    public void testTraditionalStringInvalid(Blackhole blackhole) {
        try {
            if (invalidUsername == null || invalidUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (invalidUsername.length() < 3 || invalidUsername.length() > 20) {
                throw new RuntimeException("Username is required");
            }
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 7: 传统 if-throw 方式 - 数值验证（有效数据）
    @Benchmark
    public void testTraditionalNumberValid(Blackhole blackhole) {
        try {
            if (validAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 8: 传统 if-throw 方式 - 数值验证（无效数据）
    @Benchmark
    public void testTraditionalNumberInvalid(Blackhole blackhole) {
        try {
            if (invalidAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TypedValidatorBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}