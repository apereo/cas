package org.apereo.cas.util.spring;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RefreshableHandlerInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshableHandlerInterceptorTests.RefreshableHandlerInterceptorTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RefreshableHandlerInterceptorTests {
    @Autowired
    @Qualifier("localeChangeHandlerInterceptor")
    private ObjectProvider<HandlerInterceptor> localeChangeHandlerInterceptor;

    @Test
    void verifyOperation() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();

        val proxy = new RefreshableHandlerInterceptor(localeChangeHandlerInterceptor);
        proxy.preHandle(request, response, new Object());
        assertNotNull(request.getAttribute("preHandle"));

        proxy.postHandle(request, response, new Object(), null);
        assertNotNull(request.getAttribute("postHandle"));

        proxy.afterCompletion(request, response, null, new RuntimeException());
        assertNotNull(request.getAttribute("afterCompletion"));
    }

    @Test
    void verifySupplierOperation() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();

        val proxy = new RefreshableHandlerInterceptor(() -> List.of(localeChangeHandlerInterceptor.getObject()));
        proxy.preHandle(request, response, new Object());
        assertNotNull(request.getAttribute("preHandle"));

        proxy.postHandle(request, response, new Object(), null);
        assertNotNull(request.getAttribute("postHandle"));

        proxy.afterCompletion(request, response, null, new RuntimeException());
        assertNotNull(request.getAttribute("afterCompletion"));
    }


    @TestConfiguration(value = "RefreshableHandlerInterceptorTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RefreshableHandlerInterceptorTestConfiguration {

        @Bean
        public HandlerInterceptor localeChangeHandlerInterceptor() {
            return new HandlerInterceptor() {
                @Override
                public boolean preHandle(
                    @NonNull final HttpServletRequest request,
                    @NonNull final HttpServletResponse response,
                    @NonNull final Object handler) {
                    request.setAttribute("preHandle", true);
                    return true;
                }

                @Override
                public void postHandle(
                    @NonNull final HttpServletRequest request,
                    @NonNull final HttpServletResponse response,
                    @NonNull final Object handler, final ModelAndView modelAndView) {
                    request.setAttribute("postHandle", true);
                }

                @Override
                public void afterCompletion(
                    @NonNull final HttpServletRequest request,
                    @NonNull final HttpServletResponse response,
                    @NonNull final Object handler, final Exception ex) {
                    request.setAttribute("afterCompletion", true);
                }
            };
        }
    }
}
