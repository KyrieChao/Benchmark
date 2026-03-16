package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * AOP 验证性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class AopValidationBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final String validEmail = "test@example.com";
    private static final String invalidEmail = "invalid-email";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    // 模拟服务类
    private static class UserService {

        // 无验证的方法
        public void createUserWithoutValidation(String username, String email, int age) {
            // 业务逻辑模拟
        }

        // 有验证的方法（手动验证，模拟AOP验证的开销）
        public void createUserWithValidation(String username, String email, int age) {
            // 模拟AOP验证的开销
            try {
                Failure.begin()
                        .notBlank(username, ResponseCode.of(40001, "Username is required"))
                        .email(email, ResponseCode.of(40002, "Email is invalid"))
                        .greaterOrEqual(age, 18, ResponseCode.of(40003, "Age is too young"))
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
            // 业务逻辑模拟
        }
    }

    // 自定义验证器
    private static class CustomValidator implements FastValidator<Object> {
        @Override
        public void validate(Object target, ValidationContext context) {
            // 简单验证逻辑
            if (target == null) {
                context.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("开始 AOP 验证性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        UserService service = new UserService();

        // 测试 1: 无验证方法
        testWithoutValidation(service);

        // 测试 2: 有验证方法（有效数据）
        testWithValidationValid(service);

        // 测试 3: 有验证方法（无效数据）
        testWithValidationInvalid(service);

        System.out.println("=======================================");
        System.out.println("AOP 验证性能测试完成！");
    }

    // 测试 1: 无验证方法
    private static void testWithoutValidation(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithoutValidation(validUsername, validEmail, validAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            service.createUserWithoutValidation(validUsername, validEmail, validAge);
            sink++;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("无验证方法: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 2: 有验证方法（有效数据）
    private static void testWithValidationValid(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithValidation(validUsername, validEmail, validAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            service.createUserWithValidation(validUsername, validEmail, validAge);
            sink++;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("有验证方法（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 3: 有验证方法（无效数据）
    private static void testWithValidationInvalid(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithValidation(invalidUsername, invalidEmail, invalidAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                service.createUserWithValidation(invalidUsername, invalidEmail, invalidAge);
                sink++;
            } catch (Exception e) {
                // 忽略异常
                sink--;
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("有验证方法（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }
}
