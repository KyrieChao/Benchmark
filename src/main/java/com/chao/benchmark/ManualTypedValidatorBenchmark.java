package com.chao.benchmark;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.validator.TypedValidator;

/**
 * 手动测试 TypedValidator 性能的类
 * 用于验证修复后的性能表现
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ManualTypedValidatorBenchmark {

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
        System.out.println("开始手动测试 TypedValidator 性能...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        // 初始化验证器
        CustomTypedValidator typedValidator = new CustomTypedValidator();
        // 初始化验证上下文
        FastValidator.ValidationContext context = new FastValidator.ValidationContext(true);

        // 测试 1: TypedValidator - 字符串验证（有效数据）
        testTypedValidatorStringValid(typedValidator, context);

        // 测试 2: TypedValidator - 字符串验证（无效数据）
        testTypedValidatorStringInvalid(typedValidator, context);

        // 测试 3: TypedValidator - 数值验证（有效数据）
        testTypedValidatorNumberValid(typedValidator, context);

        // 测试 4: TypedValidator - 数值验证（无效数据）
        testTypedValidatorNumberInvalid(typedValidator, context);

        System.out.println("=======================================");
        System.out.println("手动测试 TypedValidator 性能完成！");
    }

    // 测试 1: TypedValidator - 字符串验证（有效数据）
    private static void testTypedValidatorStringValid(CustomTypedValidator validator, FastValidator.ValidationContext context) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                // 重置上下文，复用对象
                context.reset();
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
    private static void testTypedValidatorStringInvalid(CustomTypedValidator validator, FastValidator.ValidationContext context) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                // 重置上下文，复用对象
                context.reset();
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
    private static void testTypedValidatorNumberValid(CustomTypedValidator validator, FastValidator.ValidationContext context) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                // 重置上下文，复用对象
                context.reset();
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
    private static void testTypedValidatorNumberInvalid(CustomTypedValidator validator, FastValidator.ValidationContext context) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                // 重置上下文，复用对象
                context.reset();
                validator.validate(invalidAge, context);
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("TypedValidator 数值验证（无效数据）: " + avgTime + " ns/op");
    }
}
