package org.apereo.cas.web.flow.configurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamicFlowModelBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Webflow")
public class DynamicFlowModelBuilderTests {
    @Test
    public void verifyOperation() {
        val builder = new DynamicFlowModelBuilder();
        builder.setStartStateId("StartStateId");
        builder.setGlobalTransitions(List.of());
        assertFalse(builder.hasFlowModelResourceChanged());
        assertNotNull(builder.getFlowModelResource());
        assertDoesNotThrow(builder::dispose);
    }

}
