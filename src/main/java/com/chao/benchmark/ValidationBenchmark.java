package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.function.LongSupplier;
import java.util.regex.Pattern;

/**
 * 性能测试类 - 测试验证框架的性能表现
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ValidationBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final String validEmail = "test@example.com";
    private static final String invalidEmail = "invalid-email";
    private static final int validAge = 20;
    private static final int invalidAge = 15;
    private static final Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    private static final int WARMUP_COUNT = 10_000;
    private static final int TEST_COUNT = 1_000_000;

    private static long failFastSimpleValid() {
        long sink = 0;
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long failFastSimpleInvalid() {
        long sink = 0;
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long failStrictSimpleValid() {
        long sink = 0;
        try {
            Failure.strict()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long failStrictSimpleInvalid() {
        long sink = 0;
        try {
            Failure.strict()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long traditionalIfThrowValid() {
        long sink = 0;
        try {
            if (validUsername == null || validUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (!emailPattern.matcher(validEmail).matches()) {
                throw new RuntimeException("Email is invalid");
            }
            if (validAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long traditionalIfThrowInvalid() {
        long sink = 0;
        try {
            if (invalidUsername == null || invalidUsername.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (!emailPattern.matcher(invalidEmail).matches()) {
                throw new RuntimeException("Email is invalid");
            }
            if (invalidAge < 18) {
                throw new RuntimeException("Age is too young");
            }
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long failFastComplexValid() {
        long sink = 0;
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .lengthMin(validUsername, 3, USERNAME_REQUIRED)
                    .lengthMax(validUsername, 20, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .lessOrEqual(validAge, 100, AGE_TOO_YOUNG)
                    .fail();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private static long failFastComplexInvalid() {
        long sink = 0;
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .lengthMin(invalidUsername, 3, USERNAME_REQUIRED)
                    .lengthMax(invalidUsername, 20, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .lessOrEqual(invalidAge, 100, AGE_TOO_YOUNG)
                    .fail();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

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
        System.out.println("开始性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("========================================");

        run("Fail-Fast 简单验证（有效数据）", ValidationBenchmark::failFastSimpleValid);
        run("Fail-Fast 简单验证（无效数据）", ValidationBenchmark::failFastSimpleInvalid);
        run("Fail-Strict 简单验证（有效数据）", ValidationBenchmark::failStrictSimpleValid);
        run("Fail-Strict 简单验证（无效数据）", ValidationBenchmark::failStrictSimpleInvalid);
        run("传统 if-throw 简单验证（有效数据）", ValidationBenchmark::traditionalIfThrowValid);
        run("传统 if-throw 简单验证（无效数据）", ValidationBenchmark::traditionalIfThrowInvalid);
        run("Fail-Fast 复杂验证（有效数据）", ValidationBenchmark::failFastComplexValid);
        run("Fail-Fast 复杂验证（无效数据）", ValidationBenchmark::failFastComplexInvalid);

        System.out.println("========================================");
        System.out.println("性能测试完成！");
    }
}
