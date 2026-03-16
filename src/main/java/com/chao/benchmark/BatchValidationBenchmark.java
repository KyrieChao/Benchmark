package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 批量验证性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class BatchValidationBenchmark {

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
    private static final int BATCH_SIZE = 100;

    // 用户对象
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
        System.out.println("开始批量验证性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("批量大小: " + BATCH_SIZE);
        System.out.println("=======================================");

        // 准备测试数据
        List<User> validUsers = new ArrayList<>();
        List<User> invalidUsers = new ArrayList<>();

        for (int i = 0; i < BATCH_SIZE; i++) {
            validUsers.add(new User(validUsername + i, validAge));
            invalidUsers.add(new User(invalidUsername, invalidAge));
        }

        // 测试 1: 单个验证（有效数据）
        testSingleValidationValid(validUsers);

        // 测试 2: 单个验证（无效数据）
        testSingleValidationInvalid(invalidUsers);

        // 测试 3: 批量验证（有效数据）
        testBatchValidationValid(validUsers);

        // 测试 4: 批量验证（无效数据）
        testBatchValidationInvalid(invalidUsers);

        // 测试 5: 并行批量验证（有效数据）
        testParallelBatchValidationValid(validUsers);

        // 测试 6: 并行批量验证（无效数据）
        testParallelBatchValidationInvalid(invalidUsers);

        System.out.println("=======================================");
        System.out.println("批量验证性能测试完成！");
    }

    // 测试 1: 单个验证（有效数据）
    private static void testSingleValidationValid(List<User> users) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT / users.size(); i++) {
            for (User user : users) {
                try {
                    Failure.begin()
                            .notBlank(user.getUsername(), USERNAME_REQUIRED)
                            .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                            .fail();
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) (TEST_COUNT);
        System.out.println("单个验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: 单个验证（无效数据）
    private static void testSingleValidationInvalid(List<User> users) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT / users.size(); i++) {
            for (User user : users) {
                try {
                    Failure.begin()
                            .notBlank(user.getUsername(), USERNAME_REQUIRED)
                            .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                            .fail();
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) (TEST_COUNT);
        System.out.println("单个验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: 批量验证（有效数据）
    private static void testBatchValidationValid(List<User> users) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT / users.size(); i++) {
            try {
                // 批量验证：使用一个验证链验证多个用户
                var validation = Failure.strict();
                for (User user : users) {
                    validation.notBlank(user.getUsername(), USERNAME_REQUIRED)
                             .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG);
                }
                validation.failAll();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) (TEST_COUNT);
        System.out.println("批量验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: 批量验证（无效数据）
    private static void testBatchValidationInvalid(List<User> users) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT / users.size(); i++) {
            try {
                var validation = Failure.strict();
                for (User user : users) {
                    validation.notBlank(user.getUsername(), USERNAME_REQUIRED)
                             .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG);
                }
                validation.failAll();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) (TEST_COUNT);
        System.out.println("批量验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 5: 并行批量验证（有效数据）
    private static void testParallelBatchValidationValid(List<User> users) {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        int loops = TEST_COUNT / users.size();
        int loopsPerThread = Math.max(1, loops / threadCount);

        long start = System.nanoTime();
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < loopsPerThread; i++) {
                        var validation = Failure.strict();
                        for (User user : users) {
                            validation.notBlank(user.getUsername(), USERNAME_REQUIRED)
                                     .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG);
                        }
                        validation.failAll();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long end = System.nanoTime();
        long ops = (long) loopsPerThread * threadCount * users.size();
        double avgTime = (end - start) / (double) ops;
        System.out.println("并行批量验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 6: 并行批量验证（无效数据）
    private static void testParallelBatchValidationInvalid(List<User> users) {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        int loops = TEST_COUNT / users.size();
        int loopsPerThread = Math.max(1, loops / threadCount);

        long start = System.nanoTime();
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < loopsPerThread; i++) {
                        var validation = Failure.strict();
                        for (User user : users) {
                            validation.notBlank(user.getUsername(), USERNAME_REQUIRED)
                                     .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG);
                        }
                        validation.failAll();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long end = System.nanoTime();
        long ops = (long) loopsPerThread * threadCount * users.size();
        double avgTime = (end - start) / (double) ops;
        System.out.println("并行批量验证（无效数据）: " + avgTime + " ns/op");
    }
}
