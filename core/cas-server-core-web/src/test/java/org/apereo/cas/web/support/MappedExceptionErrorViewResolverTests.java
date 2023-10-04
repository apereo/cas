package org.apereo.cas.web.support;

import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MappedExceptionErrorViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Web")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreWebConfiguration.class
})
public class MappedExceptionErrorViewResolverTests {
    @Autowired
    @Qualifier("defaultMappedExceptionErrorViewResolver")
    private ErrorViewResolver defaultMappedExceptionErrorViewResolver;

    @Test
    void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        var result = defaultMappedExceptionErrorViewResolver.resolveErrorView(request, HttpStatus.FORBIDDEN, Map.of());
        assertNull(result);

        request.setAttribute("jakarta.servlet.error.exception", UnauthorizedServiceException.denied("Forbidden"));
        result = defaultMappedExceptionErrorViewResolver.resolveErrorView(request, HttpStatus.FORBIDDEN, Map.of());
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, result.getViewName());
        assertTrue(result.getModel().containsKey(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION));
    }
}
