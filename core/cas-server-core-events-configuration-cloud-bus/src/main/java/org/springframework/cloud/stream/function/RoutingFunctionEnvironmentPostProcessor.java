package org.springframework.cloud.stream.function;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * This is {@link RoutingFunctionEnvironmentPostProcessor}.
 * This is a modest copy of the original class found in Spring Cloud Function
 * that removes an unnecessary cast to {@link org.springframework.core.env.StandardEnvironment}.
 * The environment that is passed, in case Spring Cloud Bootstrap is defined, is
 * one that cannot be cast down to {@link org.springframework.core.env.StandardEnvironment} directly
 * and the cast generally is not needed.
 * We have no use for this class in CAS, and yet we cannot disable it using any other way
 * since it's directly defined in Spring Cloud Function's {@code spring.factories} file.
 *
 * @author Misagh Moayyed
 * @see <a href="https://github.com/spring-cloud/spring-cloud-stream/pull/3150">Pull Request</a>
 * @see <a href="https://github.com/spring-cloud/spring-cloud-stream/blob/main/core/spring-cloud-stream/src/main/java/org/springframework/cloud/stream/function/RoutingFunctionEnvironmentPostProcessor.java">Original</a>
 * @since 8.0.0
 * @deprecated Since 8.0.0
 */
@Deprecated(since = "8.0.0", forRemoval = true)
class RoutingFunctionEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
    }
}
