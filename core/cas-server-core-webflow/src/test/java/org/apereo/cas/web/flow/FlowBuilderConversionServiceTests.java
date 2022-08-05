package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.convert.ConversionExecutorNotFoundException;
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
    @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
    protected FlowBuilderServices flowBuilderServices;

    @Test
    public void verfyConversion() {
        val conversionService = flowBuilderServices.getConversionService();

        var result = conversionService.executeConversion("https://github.io", Service.class);
        assertTrue(result instanceof Service);

        result = conversionService.executeConversion(StringToCharArrayConverter.ID, "Mellon", char[].class);
        assertTrue(result instanceof char[]);

        assertThrows(ConversionExecutorNotFoundException.class,
            () -> conversionService.executeConversion("unknown", "Mellon", char[].class));
    }
}
