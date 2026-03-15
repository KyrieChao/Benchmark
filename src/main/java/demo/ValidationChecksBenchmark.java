package demo;

import com.chao.failfast.internal.check.NumberChecks;
import com.chao.failfast.internal.check.StringChecks;

/**
 * 验证检查性能测试 - 只测试验证条件的执行
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ValidationChecksBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final String validEmail = "test@example.com";
    private static final String invalidEmail = "invalid-email";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    public static void main(String[] args) {
        System.out.println("开始验证检查性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("========================================");

        // 测试 1: 字符串非空检查（有效数据）
        testNotBlankValid();

        // 测试 2: 字符串非空检查（无效数据）
        testNotBlankInvalid();

        // 测试 3: 邮箱格式检查（有效数据）
        testEmailValid();

        // 测试 4: 邮箱格式检查（无效数据）
        testEmailInvalid();

        // 测试 5: 数值大于等于检查（有效数据）
        testGreaterOrEqualValid();

        // 测试 6: 数值大于等于检查（无效数据）
        testGreaterOrEqualInvalid();

        // 测试 7: 字符串长度最小检查（有效数据）
        testLengthMinValid();

        // 测试 8: 字符串长度最小检查（无效数据）
        testLengthMinInvalid();

        System.out.println("========================================");
        System.out.println("验证检查性能测试完成！");
    }

    // 测试 1: 字符串非空检查（有效数据）
    private static void testNotBlankValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = StringChecks.notBlank(validUsername);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("字符串非空检查（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: 字符串非空检查（无效数据）
    private static void testNotBlankInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = StringChecks.notBlank(invalidUsername);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("字符串非空检查（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: 邮箱格式检查（有效数据）
    private static void testEmailValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = StringChecks.email(validEmail);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("邮箱格式检查（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: 邮箱格式检查（无效数据）
    private static void testEmailInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = StringChecks.email(invalidEmail);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("邮箱格式检查（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 5: 数值大于等于检查（有效数据）
    private static void testGreaterOrEqualValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = NumberChecks.greaterOrEqual(validAge, 18);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("数值大于等于检查（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 6: 数值大于等于检查（无效数据）
    private static void testGreaterOrEqualInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = NumberChecks.greaterOrEqual(invalidAge, 18);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("数值大于等于检查（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 7: 字符串长度最小检查（有效数据）
    private static void testLengthMinValid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = StringChecks.lengthMin(validUsername, 3);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("字符串长度最小检查（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 8: 字符串长度最小检查（无效数据）
    private static void testLengthMinInvalid() {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            boolean result = StringChecks.lengthMin(invalidUsername, 3);
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("字符串长度最小检查（无效数据）: " + avgTime + " ns/op");
    }
}
