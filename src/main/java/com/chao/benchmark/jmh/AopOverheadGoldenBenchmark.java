package com.chao.benchmark.jmh;

import com.chao.failfast.annotation.FastValidator;
import com.chao.failfast.annotation.Validate;
import com.chao.failfast.aspect.ValidationAspect;
import com.chao.failfast.internal.check.NumberChecks;
import com.chao.failfast.internal.check.StringChecks;
import com.chao.failfast.internal.core.ResponseCode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@JmhConfig
@BenchmarkMode(Mode.AverageTime)
public class AopOverheadGoldenBenchmark {
    @State(Scope.Benchmark)
    public static class Ctx {
        private ConfigurableApplicationContext context;
        private PlainService plain;
        private NoAspectService noAspect;
        private ValidatedService validated;
        private User userValid;

        @Setup(Level.Trial)
        public void setupTrial() {
            context = new SpringApplicationBuilder(AppConfig.class)
                    .web(WebApplicationType.NONE)
                    .run();
            plain = new PlainService();
            noAspect = context.getBean(NoAspectService.class);
            validated = context.getBean(ValidatedService.class);
            userValid = new User("testuser", "test@example.com", 20);
        }

        @TearDown(Level.Trial)
        public void tearDownTrial() {
            if (context != null) context.close();
        }
    }

    public record User(String username, String email, int age) {
    }

    public static class PlainService {
        public int handle(User user) {
            return user.age();
        }
    }

    public static class NoAspectService {
        public int handle(User user) {
            return user.age();
        }
    }

    public interface ValidatedService {
        int empty(User user);

        int light(User user);
    }

    public static class ValidatedServiceImpl implements ValidatedService {
        @Override
        @Validate
        public int empty(User user) {
            return user.age();
        }

        @Override
        @Validate(value = {LightValidator.class}, fast = true)
        public int light(User user) {
            return user.age();
        }
    }

    public static class LightValidator implements FastValidator<User> {
        private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
        private static final ResponseCode EMAIL_INVALID = ResponseCode.of(40002, "Email is invalid");
        private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");

        @Override
        public void validate(User target, ValidationContext context) {
            if (!StringChecks.notBlank(target.username())) {
                context.reportError(USERNAME_REQUIRED);
            }
            if (!StringChecks.email(target.email())) {
                context.reportError(EMAIL_INVALID);
            }
            if (!NumberChecks.greaterOrEqual(target.age(), 18)) {
                context.reportError(AGE_TOO_YOUNG);
            }
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    public static class AppConfig {
        @Bean
        public NoAspectService noAspectService() {
            return new NoAspectService();
        }

        @Bean
        public ValidatedService validatedService() {
            return new ValidatedServiceImpl();
        }

        @Bean
        public ValidationAspect validationAspect() {
            return new ValidationAspect();
        }
    }

    @Benchmark
    public int baseline_plainObject(Ctx ctx) {
        return ctx.plain.handle(ctx.userValid);
    }

    @Benchmark
    public int baseline_springBean_noAspect(Ctx ctx) {
        return ctx.noAspect.handle(ctx.userValid);
    }

    @Benchmark
    public int aop_validate_empty(Ctx ctx) {
        return ctx.validated.empty(ctx.userValid);
    }

    @Benchmark
    public int aop_validate_light(Ctx ctx) {
        return ctx.validated.light(ctx.userValid);
    }
}
