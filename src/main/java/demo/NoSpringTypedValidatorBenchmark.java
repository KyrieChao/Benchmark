package demo;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;

/**
 * 无 Spring 依赖的 TypedValidator 性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class NoSpringTypedValidatorBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

    // 响应码（使用不依赖 Spring 的构造方式）
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    // 测试次数
    private static final int TEST_COUNT = 1000000;

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

    public static void main(String[] args) {
        System.out.println("开始无 Spring 依赖的 TypedValidator 性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        // 初始化验证器
        CustomTypedValidator typedValidator = new CustomTypedValidator();

        // 测试 1: TypedValidator - 字符串验证（有效数据）
        testTypedValidatorStringValid(typedValidator);

        // 测试 3: TypedValidator - 数值验证（有效数据）
        testTypedValidatorNumberValid(typedValidator);

        // 测试 5: 传统 if-throw 方式 - 字符串验证（有效数据）
        testTraditionalStringValid();

        // 测试 6: 传统 if-throw 方式 - 字符串验证（无效数据）
        testTraditionalStringInvalid();

        // 测试 7: 传统 if-throw 方式 - 数值验证（有效数据）
        testTraditionalNumberValid();

        // 测试 8: 传统 if-throw 方式 - 数值验证（无效数据）
        testTraditionalNumberInvalid();

        System.out.println("=======================================");
        System.out.println("无 Spring 依赖的 TypedValidator 性能测试完成！");
    }

    // 测试 1: TypedValidator - 字符串验证（有效数据）
    private static void testTypedValidatorStringValid(CustomTypedValidator validator) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
                validator.validate(validUsername, context);
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("TypedValidator 字符串验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: TypedValidator - 字符串验证（无效数据）
    private static void testTypedValidatorStringInvalid(CustomTypedValidator validator) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
                // 使用无效数据，触发验证失败
                validator.validate(invalidUsername, context);
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("TypedValidator 字符串验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: TypedValidator - 数值验证（有效数据）
    private static void testTypedValidatorNumberValid(CustomTypedValidator validator) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
                validator.validate(validAge, context);
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("TypedValidator 数值验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: TypedValidator - 数值验证（无效数据）
    private static void testTypedValidatorNumberInvalid(CustomTypedValidator validator) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);
                // 使用无效数据，触发验证失败
                validator.validate(invalidAge, context);
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("TypedValidator 数值验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 5: 传统 if-throw 方式 - 字符串验证（有效数据）
    private static void testTraditionalStringValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                if (validUsername == null || validUsername.isBlank()) {
                    throw new RuntimeException("Username is required");
                }
                if (validUsername.length() < 3 || validUsername.length() > 20) {
                    throw new RuntimeException("Username is required");
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("传统 if-throw 字符串验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 6: 传统 if-throw 方式 - 字符串验证（无效数据）
    private static void testTraditionalStringInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                if (invalidUsername == null || invalidUsername.isBlank()) {
                    throw new RuntimeException("Username is required");
                }
                if (invalidUsername.length() < 3 || invalidUsername.length() > 20) {
                    throw new RuntimeException("Username is required");
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("传统 if-throw 字符串验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 7: 传统 if-throw 方式 - 数值验证（有效数据）
    private static void testTraditionalNumberValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                if (validAge < 18) {
                    throw new RuntimeException("Age is too young");
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("传统 if-throw 数值验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 8: 传统 if-throw 方式 - 数值验证（无效数据）
    private static void testTraditionalNumberInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                if (invalidAge < 18) {
                    throw new RuntimeException("Age is too young");
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("传统 if-throw 数值验证（无效数据）: " + avgTime + " ns/op");
    }
}