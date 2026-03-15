package demo;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * 验证规则组合性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ValidationRulesCombinationBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final String validEmail = "test@example.com";
    private static final String invalidEmail = "invalid-email";
    private static final int validAge = 20;
    private static final int invalidAge = 15;
    private static final String validPassword = "Password123";
    private static final String invalidPassword = "pass";

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");
    private static final ResponseCode PASSWORD_WEAK = ResponseCode.of(40004, "Password is weak");

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    public static void main(String[] args) {
        System.out.println("开始验证规则组合性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        // 测试 1: 少量验证规则（有效数据）
        testFewRulesValid();

        // 测试 2: 少量验证规则（无效数据） - 暂时注释，避免 Spring 依赖问题
        // testFewRulesInvalid();

        // 测试 3: 中量验证规则（有效数据）
        testMediumRulesValid();

        // 测试 4: 中量验证规则（无效数据） - 暂时注释，避免 Spring 依赖问题
        // testMediumRulesInvalid();

        // 测试 5: 大量验证规则（有效数据）
        testManyRulesValid();

        // 测试 6: 大量验证规则（无效数据） - 暂时注释，避免 Spring 依赖问题
        // testManyRulesInvalid();

        System.out.println("=======================================");
        System.out.println("验证规则组合性能测试完成！");
    }

    // 测试 1: 少量验证规则（有效数据）
    private static void testFewRulesValid() {
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
        System.out.println("少量验证规则（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: 少量验证规则（无效数据）
    private static void testFewRulesInvalid() {
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
        System.out.println("少量验证规则（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: 中量验证规则（有效数据）
    private static void testMediumRulesValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(validUsername, USERNAME_REQUIRED)
                        .lengthMin(validUsername, 3, USERNAME_REQUIRED)
                        .lengthMax(validUsername, 20, USERNAME_REQUIRED)
                        .email(validEmail, EMAIL_INVALID)
                        .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("中量验证规则（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: 中量验证规则（无效数据）
    private static void testMediumRulesInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(invalidUsername, USERNAME_REQUIRED)
                        .lengthMin(invalidUsername, 3, USERNAME_REQUIRED)
                        .lengthMax(invalidUsername, 20, USERNAME_REQUIRED)
                        .email(invalidEmail, EMAIL_INVALID)
                        .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("中量验证规则（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 5: 大量验证规则（有效数据）
    private static void testManyRulesValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(validUsername, USERNAME_REQUIRED)
                        .lengthMin(validUsername, 3, USERNAME_REQUIRED)
                        .lengthMax(validUsername, 20, USERNAME_REQUIRED)
                        .email(validEmail, EMAIL_INVALID)
                        .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                        .lessOrEqual(validAge, 100, AGE_TOO_YOUNG)
                        .notBlank(validPassword, PASSWORD_WEAK)
                        .lengthMin(validPassword, 8, PASSWORD_WEAK)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("大量验证规则（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 6: 大量验证规则（无效数据）
    private static void testManyRulesInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(invalidUsername, USERNAME_REQUIRED)
                        .lengthMin(invalidUsername, 3, USERNAME_REQUIRED)
                        .lengthMax(invalidUsername, 20, USERNAME_REQUIRED)
                        .email(invalidEmail, EMAIL_INVALID)
                        .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                        .lessOrEqual(invalidAge, 100, AGE_TOO_YOUNG)
                        .notBlank(invalidPassword, PASSWORD_WEAK)
                        .lengthMin(invalidPassword, 8, PASSWORD_WEAK)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("大量验证规则（无效数据）: " + avgTime + " ns/op");
    }
}