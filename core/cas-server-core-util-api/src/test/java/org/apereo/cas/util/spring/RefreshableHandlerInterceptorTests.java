package org.apereo.cas.util.spring;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    RefreshableHandlerInterceptorTests.RefreshableHandlerInterceptorTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RefreshableHandlerInterceptorTests {
    @Autowired
    @Qualifier("localeChangeHandlerInterceptor")
    private ObjectProvider<HandlerInterceptor> localeChangeHandlerInterceptor;

    @Test
    public void verifyOperation() throws Exception {
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


    @TestConfiguration(value = "RefreshableHandlerInterceptorTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RefreshableHandlerInterceptorTestConfiguration {

        @Bean
        public HandlerInterceptor localeChangeHandlerInterceptor() {
            return new HandlerInterceptor() {
                @Override
                public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
                    request.setAttribute("preHandle", true);
                    return true;
                }

                @Override
                public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                                       final Object handler, final ModelAndView modelAndView) {
                    request.setAttribute("postHandle", true);
                }

                @Override
                public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                            final Object handler, final Exception ex) {
                    request.setAttribute("afterCompletion", true);
                }
            };
        }
    }
}
