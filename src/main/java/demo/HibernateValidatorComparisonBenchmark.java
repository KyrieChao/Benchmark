package demo;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Hibernate Validator 对比测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(0)
@State(Scope.Thread)
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
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

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

    @Setup
    public void setup() {
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

    // 测试 1: Fail-Fast 验证（有效数据）
    @Benchmark
    public void testFailFastValid(Blackhole blackhole) {
        try {
            Failure.begin()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .fail();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 2: Fail-Fast 验证（无效数据）
    @Benchmark
    public void testFailFastInvalid(Blackhole blackhole) {
        try {
            Failure.begin()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .fail();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 3: Fail-Strict 验证（有效数据）
    @Benchmark
    public void testFailStrictValid(Blackhole blackhole) {
        try {
            Failure.strict()
                    .notBlank(validUsername, USERNAME_REQUIRED)
                    .email(validEmail, EMAIL_INVALID)
                    .greaterOrEqual(validAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 4: Fail-Strict 验证（无效数据）
    @Benchmark
    public void testFailStrictInvalid(Blackhole blackhole) {
        try {
            Failure.strict()
                    .notBlank(invalidUsername, USERNAME_REQUIRED)
                    .email(invalidEmail, EMAIL_INVALID)
                    .greaterOrEqual(invalidAge, 18, AGE_TOO_YOUNG)
                    .failAll();
            blackhole.consume(true);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 5: Hibernate Validator 验证（有效数据）
    @Benchmark
    public void testHibernateValidatorValid(Blackhole blackhole) {
        try {
            Set<ConstraintViolation<HibernateUser>> violations = validator.validate(validUser);
            blackhole.consume(violations);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    // 测试 6: Hibernate Validator 验证（无效数据）
    @Benchmark
    public void testHibernateValidatorInvalid(Blackhole blackhole) {
        try {
            Set<ConstraintViolation<HibernateUser>> violations = validator.validate(invalidUser);
            blackhole.consume(violations);
        } catch (Exception e) {
            blackhole.consume(e);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HibernateValidatorComparisonBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
