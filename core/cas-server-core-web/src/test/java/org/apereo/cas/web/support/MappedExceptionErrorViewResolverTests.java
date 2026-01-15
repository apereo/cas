package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MappedExceptionErrorViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreWebAutoConfiguration.class)
class MappedExceptionErrorViewResolverTests {
    @Autowired
    @Qualifier("defaultMappedExceptionErrorViewResolver")
    private ErrorViewResolver defaultMappedExceptionErrorViewResolver;

    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        var result = defaultMappedExceptionErrorViewResolver.resolveErrorView(request, HttpStatus.FORBIDDEN, Map.of());
        assertNull(result);

        request.setAttribute("jakarta.servlet.error.exception", UnauthorizedServiceException.denied("Forbidden"));
        result = defaultMappedExceptionErrorViewResolver.resolveErrorView(request, HttpStatus.FORBIDDEN, Map.of());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getViewName());
        assertTrue(result.getModel().containsKey(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }
}
