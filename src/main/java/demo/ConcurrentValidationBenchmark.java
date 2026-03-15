package demo;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 并发验证性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ConcurrentValidationBenchmark {

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

    public static void main(String[] args) {
        System.out.println("开始并发验证性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        // 测试不同线程数的并发性能
        int[] threadCounts = {1, 2, 4, 8, 16};
        for (int threadCount : threadCounts) {
            System.out.println("测试线程数: " + threadCount);
            testConcurrentValidation(threadCount, true);  // 有效数据
            // testConcurrentValidation(threadCount, false); // 无效数据 - 暂时注释，避免 Spring 依赖问题
            System.out.println();
        }

        System.out.println("=======================================");
        System.out.println("并发验证性能测试完成！");
    }

    // 并发验证测试
    private static void testConcurrentValidation(int threadCount, boolean valid) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 预热阶段
        for (int i = 0; i < 1000; i++) {
            // 只预热有效数据的验证，避免 Spring 依赖问题
            validateValidData();
        }

        long start = System.nanoTime();

        int operationsPerThread = TEST_COUNT / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (valid) {
                            validateValidData();
                        } else {
                            validateInvalidData();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;

        if (valid) {
            System.out.println("  有效数据: " + avgTime + " ns/op");
        } else {
            System.out.println("  无效数据: " + avgTime + " ns/op");
        }

        executor.shutdown();
    }

    // 验证有效数据
    private static void validateValidData() {
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
        } catch (Exception e) {
            // 忽略异常
        }
    }

    // 验证无效数据
    private static void validateInvalidData() {
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .fail();
        } catch (Exception e) {
            // 忽略异常
        }
    }
}