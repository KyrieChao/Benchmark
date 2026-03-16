package com.chao.benchmark.jmh;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.Chain;
import com.chao.failfast.internal.core.ResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.regex.Pattern;

@JmhConfig
@BenchmarkMode(Mode.AverageTime)
public class ValidationGoldenBenchmark {
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    @State(Scope.Thread)
    public static class Input {
        @Param({"VALID", "FAIL_FIRST", "FAIL_MIDDLE", "FAIL_LAST"})
        public String failPosition;

        @Param({"1", "10", "50"})
        public int invalidRatePercent;

        private boolean[] invalidFlags;
        private int flagIndex;

        public String username;
        public String email;
        public int age;

        private String validUsername;
        private String invalidUsername;
        private String validEmail;
        private String invalidEmail;
        private int validAge;
        private int invalidAge;

        private final List<ResponseCode> errors = new ArrayList<>(4);

        private ValidatorFactory hvFactory;
        private Validator hvValidator;
        private ValidatorFactory hvFailFastFactory;
        private Validator hvFailFastValidator;

        private HvUser hvUser;

        @Setup(Level.Trial)
        public void setupTrial() {
            validUsername = "testuser";
            invalidUsername = "";
            validEmail = "test@example.com";
            invalidEmail = "invalid-email";
            validAge = 20;
            invalidAge = 15;

            SplittableRandom r = new SplittableRandom(1234567L);
            invalidFlags = new boolean[1024];
            for (int i = 0; i < invalidFlags.length; i++) {
                invalidFlags[i] = r.nextInt(100) < invalidRatePercent;
            }
            flagIndex = 0;

            hvFactory = Validation.buildDefaultValidatorFactory();
            hvValidator = hvFactory.getValidator();

            hvFailFastFactory = Validation.byDefaultProvider()
                    .configure()
                    .addProperty("hibernate.validator.fail_fast", "true")
                    .buildValidatorFactory();
            hvFailFastValidator = hvFailFastFactory.getValidator();

            hvUser = new HvUser(validUsername, validEmail, validAge);
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            flagIndex = 0;
        }

        @TearDown(Level.Trial)
        public void tearDownTrial() {
            close();
        }

        public void nextInput() {
            boolean invalid = invalidFlags[flagIndex++ & (invalidFlags.length - 1)];
            if (!invalid) {
                username = validUsername;
                email = validEmail;
                age = validAge;
                hvUser.username = validUsername;
                hvUser.email = validEmail;
                hvUser.age = validAge;
                return;
            }

            switch (failPosition) {
                case "FAIL_FIRST" -> {
                    username = invalidUsername;
                    email = validEmail;
                    age = validAge;
                }
                case "FAIL_MIDDLE" -> {
                    username = validUsername;
                    email = invalidEmail;
                    age = validAge;
                }
                case "FAIL_LAST" -> {
                    username = validUsername;
                    email = validEmail;
                    age = invalidAge;
                }
                default -> {
                    username = invalidUsername;
                    email = invalidEmail;
                    age = invalidAge;
                }
            }

            hvUser.username = username;
            hvUser.email = email;
            hvUser.age = age;
        }

        public void close() {
            if (hvFactory != null) hvFactory.close();
            if (hvFailFastFactory != null) hvFailFastFactory.close();
            hvFactory = null;
            hvValidator = null;
            hvFailFastFactory = null;
            hvFailFastValidator = null;
        }
    }

    private static class HvUser {
        @NotBlank(message = "Username is required")
        private String username;

        @Email(message = "Email is invalid")
        private String email;

        @Min(value = 18, message = "Age is too young")
        private int age;

        private HvUser(String username, String email, int age) {
            this.username = username;
            this.email = email;
            this.age = age;
        }
    }

    @Benchmark
    public int failureFailFast_isValid(Input in) {
        in.nextInput();
        Chain chain = Failure.begin();
        chain.notBlank(in.username, USERNAME_REQUIRED)
                .email(in.email, EMAIL_INVALID)
                .greaterOrEqual(in.age, 18, AGE_TOO_YOUNG);
        return chain.isValid() ? 1 : 0;
    }

    @Benchmark
    public int failureFailStrict_collect(Input in) {
        in.nextInput();
        Chain chain = Failure.strict();
        chain.notBlank(in.username, USERNAME_REQUIRED)
                .email(in.email, EMAIL_INVALID)
                .greaterOrEqual(in.age, 18, AGE_TOO_YOUNG);
        return chain.getCauses().size();
    }

    @Benchmark
    public int ifReturn_collectAll(Input in) {
        in.nextInput();
        List<ResponseCode> errors = in.errors;
        errors.clear();

        if (in.username == null || in.username.isBlank()) {
            errors.add(USERNAME_REQUIRED);
        }
        if (!EMAIL_PATTERN.matcher(in.email).matches()) {
            errors.add(EMAIL_INVALID);
        }
        if (in.age < 18) {
            errors.add(AGE_TOO_YOUNG);
        }

        return errors.size();
    }

    @Benchmark
    public int ifThrow_failFast(Input in) {
        in.nextInput();
        try {
            if (in.username == null || in.username.isBlank()) {
                throw new RuntimeException("Username is required");
            }
            if (!EMAIL_PATTERN.matcher(in.email).matches()) {
                throw new RuntimeException("Email is invalid");
            }
            if (in.age < 18) {
                throw new RuntimeException("Age is too young");
            }
            return 1;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    @Benchmark
    public int hibernateValidator_default(Input in) {
        in.nextInput();
        Set<ConstraintViolation<HvUser>> violations = in.hvValidator.validate(in.hvUser);
        return violations.size();
    }

    @Benchmark
    public int hibernateValidator_failFast(Input in) {
        in.nextInput();
        Set<ConstraintViolation<HvUser>> violations = in.hvFailFastValidator.validate(in.hvUser);
        return violations.size();
    }
}
