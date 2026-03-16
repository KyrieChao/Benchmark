package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * Spring 集成性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class SpringIntegrationBenchmark {

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

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    // 模拟服务类
    private static class UserService {

        // 原生验证方法
        public void createUserWithNativeValidation(String username, String email, int age) {
            try {
                Failure.begin()
                        .notBlank(username, USERNAME_REQUIRED)
                        .email(email, EMAIL_INVALID)
                        .greaterOrEqual(age, 18, AGE_TOO_YOUNG)
                        .fail();
                // 业务逻辑
            } catch (Exception e) {
                // 异常处理
            }
        }

        // 模拟 Spring AOP 验证方法
        public void createUserWithAopValidation(String username, String email, int age) {
            try {
                // 验证逻辑
                Failure.begin()
                        .notBlank(username, USERNAME_REQUIRED)
                        .email(email, EMAIL_INVALID)
                        .greaterOrEqual(age, 18, AGE_TOO_YOUNG)
                        .fail();
                // 业务逻辑
            } catch (Exception e) {
                // 异常处理
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("开始 Spring 集成性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        UserService service = new UserService();

        // 测试 1: 原生验证（有效数据）
        testNativeValidationValid(service);

        // 测试 2: 原生验证（无效数据）
        testNativeValidationInvalid(service);

        // 测试 3: 模拟 Spring AOP 验证（有效数据）
        testAopValidationValid(service);

        // 测试 4: 模拟 Spring AOP 验证（无效数据）
        testAopValidationInvalid(service);

        System.out.println("=======================================");
        System.out.println("Spring 集成性能测试完成！");
    }

    // 测试 1: 原生验证（有效数据）
    private static void testNativeValidationValid(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithNativeValidation(validUsername, validEmail, validAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            service.createUserWithNativeValidation(validUsername, validEmail, validAge);
            sink++;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("原生验证（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 2: 原生验证（无效数据）
    private static void testNativeValidationInvalid(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithNativeValidation(invalidUsername, invalidEmail, invalidAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            service.createUserWithNativeValidation(invalidUsername, invalidEmail, invalidAge);
            sink++;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("原生验证（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 3: 模拟 Spring AOP 验证（有效数据）
    private static void testAopValidationValid(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithAopValidation(validUsername, validEmail, validAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            service.createUserWithAopValidation(validUsername, validEmail, validAge);
            sink++;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Spring AOP 验证（有效数据）: " + avgTime + " ns/op, sink=" + sink);
    }

    // 测试 4: 模拟 Spring AOP 验证（无效数据）
    private static void testAopValidationInvalid(UserService service) {
        for (int i = 0; i < 10000; i++) {
            service.createUserWithAopValidation(invalidUsername, invalidEmail, invalidAge);
        }
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            service.createUserWithAopValidation(invalidUsername, invalidEmail, invalidAge);
            sink++;
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("Spring AOP 验证（无效数据）: " + avgTime + " ns/op, sink=" + sink);
    }
}
