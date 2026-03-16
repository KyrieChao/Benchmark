package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.function.LongSupplier;

/**
 * Hibernate Validator 对比测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class HibernateValidatorComparisonBenchmark {

    // 测试数据
    private String validUsername;
    private String invalidUsername;
    private String validEmail;
    private String invalidEmail;
    private int validAge;
    private int invalidAge;
    private HibernateUser validUser;
    private HibernateUser invalidUser;

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    // Hibernate Validator 验证器
    private ValidatorFactory factory;
    private Validator validator;
    private ValidatorFactory failFastFactory;
    private Validator failFastValidator;

    // Hibernate Validator 测试模型类
    private static class HibernateUser {
        @NotBlank(message = "Username is required")
        private String username;

        @Email(message = "Email is invalid")
        private String email;

        @Min(value = 18, message = "Age is too young")
        private int age;

        public HibernateUser(String username, String email, int age) {
            this.username = username;
            this.email = email;
            this.age = age;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public int getAge() {
            return age;
        }
    }

    public void setup() {
        if (factory == null) {
            factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        }
        if (failFastFactory == null) {
            failFastFactory = Validation.byDefaultProvider()
                    .configure()
                    .addProperty("hibernate.validator.fail_fast", "true")
                    .buildValidatorFactory();
            failFastValidator = failFastFactory.getValidator();
        }
        // 准备测试数据
        validUsername = "testuser";
        invalidUsername = "";
        validEmail = "test@example.com";
        invalidEmail = "invalid-email";
        validAge = 20;
        invalidAge = 15;
        validUser = new HibernateUser(validUsername, validEmail, validAge);
        invalidUser = new HibernateUser(invalidUsername, invalidEmail, invalidAge);
    }

    private void close() {
        if (factory != null) factory.close();
        if (failFastFactory != null) failFastFactory.close();
        factory = null;
        validator = null;
        failFastFactory = null;
        failFastValidator = null;
    }

    private long failureFailFastValid() {
        long sink = 0;
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private long failureFailFastInvalid() {
        long sink = 0;
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .fail();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private long failureFailStrictValid() {
        long sink = 0;
        try {
            Failure.strict()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private long failureFailStrictInvalid() {
        long sink = 0;
        try {
            Failure.strict()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            sink++;
        } catch (Exception e) {
            sink--;
        }
        return sink;
    }

    private long hibernateValid() {
        Set<ConstraintViolation<HibernateUser>> violations = validator.validate(validUser);
        return violations.size();
    }

    private long hibernateInvalid() {
        Set<ConstraintViolation<HibernateUser>> violations = validator.validate(invalidUser);
        return violations.size();
    }

    private long hibernateFailFastValid() {
        Set<ConstraintViolation<HibernateUser>> violations = failFastValidator.validate(validUser);
        return violations.size();
    }

    private long hibernateFailFastInvalid() {
        Set<ConstraintViolation<HibernateUser>> violations = failFastValidator.validate(invalidUser);
        return violations.size();
    }

    private static final int WARMUP_COUNT = 10_000;
    private static final int TEST_COUNT = 1_000_000;

    private static void run(String name, LongSupplier op) {
        long warm = 0;
        for (int i = 0; i < WARMUP_COUNT; i++) {
            warm ^= op.getAsLong();
        }

        long start = System.nanoTime();
        long sink = 0;
        for (int i = 0; i < TEST_COUNT; i++) {
            sink ^= op.getAsLong();
        }
        long end = System.nanoTime();

        double avg = (end - start) / (double) TEST_COUNT;
        System.out.println(name + ": " + avg + " ns/op, sink=" + sink + ", warm=" + warm);
    }

    public static void main(String[] args) {
        System.out.println("开始 Hibernate Validator 对比测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        HibernateValidatorComparisonBenchmark bench = new HibernateValidatorComparisonBenchmark();
        bench.setup();

        try {
            run("Failure Fail-Fast（有效数据）", bench::failureFailFastValid);
            run("Failure Fail-Fast（无效数据）", bench::failureFailFastInvalid);
            run("Failure Fail-Strict（有效数据）", bench::failureFailStrictValid);
            run("Failure Fail-Strict（无效数据）", bench::failureFailStrictInvalid);
            run("Hibernate Validator（有效数据）", bench::hibernateValid);
            run("Hibernate Validator（无效数据）", bench::hibernateInvalid);
            run("Hibernate Validator Fail-Fast（有效数据）", bench::hibernateFailFastValid);
            run("Hibernate Validator Fail-Fast（无效数据）", bench::hibernateFailFastInvalid);
        } finally {
            bench.close();
        }

        System.out.println("=======================================");
        System.out.println("Hibernate Validator 对比测试完成！");
}
}
