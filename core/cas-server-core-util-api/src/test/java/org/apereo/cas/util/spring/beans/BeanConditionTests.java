package org.apereo.cas.util.spring.beans;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BeanConditionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
public class BeanConditionTests {
    @Test
    public void verifyExistsOperation() {
        val env = new MockEnvironment();
        env.setProperty("cas.property.location", "classpath:/x509.crt");
        val condition = BeanCondition.on("cas.property.location")
            .exists()
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyOperation() {
        val env = new MockEnvironment();
        env.setProperty("cas.property-name.prefix", "value");
        val condition = BeanCondition.on("cas.property-name.prefix")
            .withDefaultValue("defaultValue")
            .evenIfMissing()
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyBlankValue() {
        val env = new MockEnvironment();
        env.setProperty("cas.property-name.prefix", StringUtils.EMPTY);
        val condition = BeanCondition.on("cas.property-name.prefix")
            .withDefaultValue("defaultValue")
            .given(env)
            .get();
        assertFalse(condition);
    }

    @Test
    public void verifyDefaultValue() {
        val env = new MockEnvironment();
        val condition = BeanCondition.on("cas.property-name.prefix")
            .withDefaultValue("defaultValue")
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyTrueValue() {
        val env = new MockEnvironment();
        env.setProperty("cas.property.name", "TRUE");
        val condition = BeanCondition.on("cas.property.name")
            .isTrue()
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyMatchMissing() {
        val env = new MockEnvironment();
        env.setProperty("cas.property.name", "value");
        val condition = BeanCondition.on("cas.property.other-name")
            .evenIfMissing()
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyMatchMissingFails() {
        val env = new MockEnvironment();
        env.setProperty("cas.property.name", "value");
        val condition = BeanCondition.on("cas.property.other-name")
            .given(env)
            .get();
        assertFalse(condition);
    }
}
