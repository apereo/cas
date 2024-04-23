package org.apereo.cas.util.spring;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RefreshableHandlerInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    RefreshableHandlerInterceptorTests.RefreshableHandlerInterceptorTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RefreshableHandlerInterceptorTests {
    @Autowired
    @Qualifier("localeChangeHandlerInterceptor")
    private ObjectProvider<HandlerInterceptor> localeChangeHandlerInterceptor;

    @Test
    void verifyOperation() throws Throwable {
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
    void verifySupplierOperation() throws Throwable {
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
                    @Nonnull final HttpServletRequest request,
                    @Nonnull final HttpServletResponse response,
                    @Nonnull final Object handler) {
                    request.setAttribute("preHandle", true);
                    return true;
                }

                @Override
                public void postHandle(
                    @Nonnull final HttpServletRequest request,
                    @Nonnull final HttpServletResponse response,
                    @Nonnull final Object handler, final ModelAndView modelAndView) {
                    request.setAttribute("postHandle", true);
                }

                @Override
                public void afterCompletion(
                    @Nonnull final HttpServletRequest request,
                    @Nonnull final HttpServletResponse response,
                    @Nonnull final Object handler, final Exception ex) {
                    request.setAttribute("afterCompletion", true);
                }
            };
        }
    }
}
