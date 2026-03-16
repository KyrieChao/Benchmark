package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * 国际化验证性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class I18nValidationBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final String validEmail = "test@example.com";
    private static final String invalidEmail = "invalid-email";

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode USERNAME_REQUIRED_DYNAMIC = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID_DYNAMIC = ResponseCode.of(40002, "Email is invalid");

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    public static void main(String[] args) {
        System.out.println("开始国际化验证性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        // 测试 1: 普通消息验证（有效数据）
        testNormalMessageValid();

        // 测试 2: 普通消息验证（无效数据）
        testNormalMessageInvalid();

        // 测试 3: 国际化消息验证（有效数据）
        testI18nMessageValid();

        // 测试 4: 国际化消息验证（无效数据）
        testI18nMessageInvalid();

        System.out.println("=======================================");
        System.out.println("国际化验证性能测试完成！");
    }

    // 测试 1: 普通消息验证（有效数据）
    private static void testNormalMessageValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(validUsername, USERNAME_REQUIRED)
                        .email(validEmail, EMAIL_INVALID)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("普通消息验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: 普通消息验证（无效数据）
    private static void testNormalMessageInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(invalidUsername, USERNAME_REQUIRED)
                        .email(invalidEmail, EMAIL_INVALID)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("普通消息验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: 国际化消息验证（有效数据）
    private static void testI18nMessageValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(validUsername, USERNAME_REQUIRED_DYNAMIC)
                        .email(validEmail, EMAIL_INVALID_DYNAMIC)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("国际化消息验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: 国际化消息验证（无效数据）
    private static void testI18nMessageInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(invalidUsername, USERNAME_REQUIRED_DYNAMIC)
                        .email(invalidEmail, EMAIL_INVALID_DYNAMIC)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("国际化消息验证（无效数据）: " + avgTime + " ns/op");
    }
}
