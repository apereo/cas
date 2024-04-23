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
class FlowBuilderConversionServiceTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
    protected FlowBuilderServices flowBuilderServices;

    @Test
    void verfyConversion() {
        val conversionService = flowBuilderServices.getConversionService();

        var result = conversionService.executeConversion("https://github.io", Service.class);
        assertInstanceOf(Service.class, result);

        result = conversionService.executeConversion(StringToCharArrayConverter.ID, "Mellon", char[].class);
        assertInstanceOf(char[].class, result);

        assertThrows(ConversionExecutorNotFoundException.class,
            () -> conversionService.executeConversion("unknown", "Mellon", char[].class));
    }
}
