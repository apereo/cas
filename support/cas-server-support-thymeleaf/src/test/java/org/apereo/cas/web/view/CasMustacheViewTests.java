package org.apereo.cas.web.view;

import module java.base;
import org.apereo.cas.BaseThymeleafTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.validation.CasProtocolViewFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMustacheViewTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseThymeleafTests.SharedTestConfiguration.class)
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class CasMustacheViewTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasProtocolViewFactory.BEAN_NAME_MUSTACHE_VIEW_FACTORY)
    private CasProtocolViewFactory casProtocolViewFactory;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyCas3FailureView() throws Throwable {
        renderView(casProperties.getView().getCas3().getFailure(),
            Map.of("code", CasProtocolConstants.ERROR_CODE_INVALID_REQUEST,
                "description", "Invalid request"));
    }

    @Test
    void verifyCasProxyFailureView() throws Throwable {
        renderView(casProperties.getView().getCas2().getProxy().getFailure(),
            Map.of("code", CasProtocolConstants.ERROR_CODE_INVALID_REQUEST,
                "description", "Invalid request"));
    }

    @Test
    void verifyCas2FailureView() throws Throwable {
        renderView(casProperties.getView().getCas2().getFailure(),
            Map.of("code", CasProtocolConstants.ERROR_CODE_INVALID_REQUEST,
                "description", "Invalid request"));
    }

    private void renderView(final String name, final Map model) throws UnsupportedEncodingException {
        val view = casProtocolViewFactory.create(applicationContext, name, MediaType.TEXT_HTML_VALUE);
        val response = new MockHttpServletResponse();
        assertDoesNotThrow(() -> view.render(model, new MockHttpServletRequest(), response));
        assertNotNull(response.getContentAsString());
    }


}
