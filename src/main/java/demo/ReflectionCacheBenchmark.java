package demo;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

/**
 * 反射缓存性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ReflectionCacheBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    // 测试对象
    private static class User {
        private String username;
        private int age;

        public User(String username, int age) {
            this.username = username;
            this.age = age;
        }

        public String getUsername() { return username; }
        public int getAge() { return age; }
    }

    public static void main(String[] args) {
        System.out.println("开始反射缓存性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        User validUser = new User(validUsername, validAge);
        User invalidUser = new User(invalidUsername, invalidAge);

        // 测试 1: 首次验证（有效数据）
        testFirstValidationValid(validUser);

        // 测试 2: 后续验证（有效数据）
        testSubsequentValidationValid(validUser);

        // 测试 3: 首次验证（无效数据） - 暂时注释，避免 Spring 依赖问题
        // testFirstValidationInvalid(invalidUser);

        // 测试 4: 后续验证（无效数据） - 暂时注释，避免 Spring 依赖问题
        // testSubsequentValidationInvalid(invalidUser);

        System.out.println("=======================================");
        System.out.println("反射缓存性能测试完成！");
    }

    // 测试 1: 首次验证（有效数据）
    private static void testFirstValidationValid(User user) {
        long start = System.nanoTime();
        try {
            Failure.begin()
                    .notBlank(user.getUsername(), USERNAME_REQUIRED)
                    .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                    .fail();
        } catch (Exception e) {
            // 忽略异常
        }
        long end = System.nanoTime();
        double avgTime = (end - start);
        System.out.println("首次验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: 后续验证（有效数据）
    private static void testSubsequentValidationValid(User user) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(user.getUsername(), USERNAME_REQUIRED)
                        .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("后续验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: 首次验证（无效数据）
    private static void testFirstValidationInvalid(User user) {
        long start = System.nanoTime();
        try {
            Failure.begin()
                    .notBlank(user.getUsername(), USERNAME_REQUIRED)
                    .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                    .fail();
        } catch (Exception e) {
            // 忽略异常
        }
        long end = System.nanoTime();
        double avgTime = (end - start);
        System.out.println("首次验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: 后续验证（无效数据）
    private static void testSubsequentValidationInvalid(User user) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(user.getUsername(), USERNAME_REQUIRED)
                        .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("后续验证（无效数据）: " + avgTime + " ns/op");
    }
}