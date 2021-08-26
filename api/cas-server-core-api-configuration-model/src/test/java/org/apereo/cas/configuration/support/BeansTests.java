package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BeansTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class BeansTests {

    @Test
    public void verifyOperation() {
        val props = new PrincipalAttributesProperties();
        props.getStub().setId("helloworld");
        props.getStub().getAttributes().put("name", "true");
        val input = Beans.newStubAttributeRepository(props);
        assertNotNull(input);
    }

    @Test
    public void verifyDurations() {
        assertNotNull(Beans.newDuration("0"));
        assertNotNull(Beans.newDuration("never"));
        assertNotNull(Beans.newDuration("infinite"));
        assertNotNull(Beans.newDuration("-1"));
    }
}
