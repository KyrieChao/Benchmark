package com.chao.benchmark;

import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.regex.Pattern;

/**
 * 核心性能测试类 - 只测试验证逻辑，不依赖 Spring
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class CoreValidationBenchmark {



    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final String validEmail = "test@example.com";
    private static final String invalidEmail = "invalid-email";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    public static void main(String[] args) {
        System.out.println("开始核心性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("========================================");

        // 测试 1: Fail-Fast 模式 - 简单验证（有效数据）
        testFailFastSimpleValid();

        // 测试 2: Fail-Fast 模式 - 简单验证（无效数据）
        testFailFastSimpleInvalid();

        // 测试 3: Fail-Strict 模式 - 简单验证（有效数据）
        testFailStrictSimpleValid();

        // 测试 4: Fail-Strict 模式 - 简单验证（无效数据）
        testFailStrictSimpleInvalid();

        // 测试 5: 传统 if-throw 方式 - 简单验证（有效数据）
        testTraditionalIfThrowValid();

        // 测试 6: 传统 if-throw 方式 - 简单验证（无效数据）
        testTraditionalIfThrowInvalid();

        // 测试 7: Fail-Fast 模式 - 复杂验证（有效数据）
        testFailFastComplexValid();

        // 测试 8: Fail-Fast 模式 - 复杂验证（无效数据）
        testFailFastComplexInvalid();

        System.out.println("========================================");
        System.out.println("核心性能测试完成！");
    }

    // 测试 1: Fail-Fast 模式 - 简单验证（有效数据）
    private static void testFailFastSimpleValid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            Chain chain = Chain.begin(true);
            chain.notBlank(validUsername, USERNAME_REQUIRED)
                 .email(validEmail, EMAIL_INVALID)
                 .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG);
            sink += chain.isValid() ? 1 : 0;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Fail-Fast 简单验证（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 2: Fail-Fast 模式 - 简单验证（无效数据）
    private static void testFailFastSimpleInvalid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            Chain chain = Chain.begin(true);
            chain.notBlank(invalidUsername, USERNAME_REQUIRED)
                 .email(validEmail, EMAIL_INVALID)
                 .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG);
            sink += chain.isValid() ? 1 : 0;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Fail-Fast 简单验证（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 3: Fail-Strict 模式 - 简单验证（有效数据）
    private static void testFailStrictSimpleValid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            Chain chain = Chain.begin(false);
            chain.notBlank(validUsername, USERNAME_REQUIRED)
                 .email(validEmail, EMAIL_INVALID)
                 .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG);
            sink += chain.isValid() ? 1 : 0;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Fail-Strict 简单验证（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 4: Fail-Strict 模式 - 简单验证（无效数据）
    private static void testFailStrictSimpleInvalid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            Chain chain = Chain.begin(false);
            chain.notBlank(invalidUsername, USERNAME_REQUIRED)
                 .email(invalidEmail, EMAIL_INVALID)
                 .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG);
            sink += chain.isValid() ? 1 : 0;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Fail-Strict 简单验证（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 5: 传统 if-throw 方式 - 简单验证（有效数据）
    private static void testTraditionalIfThrowValid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                if (validUsername == null || validUsername.isBlank()) {
                    throw new RuntimeException("Username is required");
                }
                if (!EMAIL_PATTERN.matcher(validEmail).matches()) {
                    throw new RuntimeException("Email is invalid");
                }
                if (validAge < 18) {
                    throw new RuntimeException("Age is too young");
                }
                sink++;
            } catch (Exception e) {
                sink--;
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("传统 if-throw 简单验证（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 6: 传统 if-throw 方式 - 简单验证（无效数据）
    private static void testTraditionalIfThrowInvalid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                if (invalidUsername == null || invalidUsername.isBlank()) {
                    throw new RuntimeException("Username is required");
                }
                if (!EMAIL_PATTERN.matcher(invalidEmail).matches()) {
                    throw new RuntimeException("Email is invalid");
                }
                if (invalidAge < 18) {
                    throw new RuntimeException("Age is too young");
                }
                sink++;
            } catch (Exception e) {
                sink--;
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("传统 if-throw 简单验证（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 7: Fail-Fast 模式 - 复杂验证（有效数据）
    private static void testFailFastComplexValid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            Chain chain = Chain.begin(true);
            chain.notBlank(validUsername, USERNAME_REQUIRED)
                 .lengthMin(validUsername, 3, USERNAME_REQUIRED)
                 .lengthMax(validUsername, 20, USERNAME_REQUIRED)
                 .email(validEmail, EMAIL_INVALID)
                 .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                 .lessOrEqual(validAge, 100, AGE_TOO_YOUNG);
            sink += chain.isValid() ? 1 : 0;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Fail-Fast 复杂验证（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 8: Fail-Fast 模式 - 复杂验证（无效数据）
    private static void testFailFastComplexInvalid() {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            Chain chain = Chain.begin(true);
            chain.notBlank(invalidUsername, USERNAME_REQUIRED)
                 .lengthMin(invalidUsername, 3, USERNAME_REQUIRED)
                 .lengthMax(invalidUsername, 20, USERNAME_REQUIRED)
                 .email(invalidEmail, EMAIL_INVALID)
                 .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                 .lessOrEqual(invalidAge, 100, AGE_TOO_YOUNG);
            sink += chain.isValid() ? 1 : 0;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Fail-Fast 复杂验证（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }
}
