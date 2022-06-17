package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FlowBuilderConversionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowConfig")
public class FlowBuilderConversionServiceTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("flowBuilderServices")
    protected FlowBuilderServices flowBuilderServices;

    @Test
    public void verfyConversion() throws Exception {
        val result = flowBuilderServices.getConversionService().executeConversion("https://github.io", Service.class);
        assertTrue(result instanceof Service);
    }
}
