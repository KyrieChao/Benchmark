package com.chao.benchmark;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;

import java.util.function.LongSupplier;

/**
 * TypedValidator 性能测试类 - 测试类型化验证器的性能表现
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class TypedValidatorBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

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

    public void setup() {
        typedValidator = new CustomTypedValidator();
        context = new FastValidator.ValidationContext(true);
    }

    private long typedValidatorStringValid() {
        context.reset();
        try {
            typedValidator.validate(validUsername, context);
            return context.isValid() ? 1 : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private long typedValidatorStringInvalid() {
        context.reset();
        try {
            typedValidator.validate(invalidUsername, context);
            return context.isValid() ? 1 : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private long typedValidatorNumberValid() {
        context.reset();
        try {
            typedValidator.validate(validAge, context);
            return context.isValid() ? 1 : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private long typedValidatorNumberInvalid() {
        context.reset();
        try {
            typedValidator.validate(invalidAge, context);
            return context.isValid() ? 1 : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private static long traditionalStringValid() {
        try {
            if (validUsername == null || validUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (validUsername.length() < 3 || validUsername.length() > 20) {
                throw new RuntimeException("Username is required");
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static long traditionalStringInvalid() {
        try {
            if (invalidUsername == null || invalidUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (invalidUsername.length() < 3 || invalidUsername.length() > 20) {
                throw new RuntimeException("Username is required");
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static long traditionalNumberValid() {
        try {
            if (validAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static long traditionalNumberInvalid() {
        try {
            if (invalidAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static final int WARMUP_COUNT = 10_000;
    private static final int TEST_COUNT = 1_000_000;

    private static void run(String name, LongSupplier op) {
        long warm = 0;
        for (int i = 0; i < WARMUP_COUNT; i++) {
            warm ^= op.getAsLong();
        }

        long start = System.nanoTime();
        long sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            sink ^= op.getAsLong();
        }
        long end = System.nanoTime();

        double avg = (end - start) / (double) TEST_COUNT;
        System.out.println(name + ": " + avg + " ns/op, sink=" + sink + ", warm=" + warm);
    }

    public static void main(String[] args) {
        System.out.println("开始 TypedValidator 性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        TypedValidatorBenchmark bench = new TypedValidatorBenchmark();
        bench.setup();

        run("TypedValidator 字符串验证（有效数据）", bench::typedValidatorStringValid);
        run("TypedValidator 字符串验证（无效数据）", bench::typedValidatorStringInvalid);
        run("TypedValidator 数值验证（有效数据）", bench::typedValidatorNumberValid);
        run("TypedValidator 数值验证（无效数据）", bench::typedValidatorNumberInvalid);
        run("传统 if-throw 字符串验证（有效数据）", TypedValidatorBenchmark::traditionalStringValid);
        run("传统 if-throw 字符串验证（无效数据）", TypedValidatorBenchmark::traditionalStringInvalid);
        run("传统 if-throw 数值验证（有效数据）", TypedValidatorBenchmark::traditionalNumberValid);
        run("传统 if-throw 数值验证（无效数据）", TypedValidatorBenchmark::traditionalNumberInvalid);

        System.out.println("=======================================");
        System.out.println("TypedValidator 性能测试完成！");
    }
}
