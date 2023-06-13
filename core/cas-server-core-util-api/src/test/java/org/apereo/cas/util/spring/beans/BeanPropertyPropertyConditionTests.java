package org.apereo.cas.util.spring.beans;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BeanPropertyPropertyConditionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
public class BeanPropertyPropertyConditionTests {

    @Test
    public void verifyExpressionLanguageEmbedded() {
        val env = new MockEnvironment();
        env.setProperty("cas.property1", "time-${#localDateTimeUtc}");
        val condition = BeanCondition.on("cas.property1")
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyExpressionLanguagePassing() {
        val env = new MockEnvironment();
        env.setProperty("cas.property1", "${#localDateTimeUtc}");
        val condition = BeanCondition.on("cas.property1")
            .given(env)
            .get();
        assertTrue(condition);
    }

    @Test
    public void verifyMultipleConditions() {
        val env = new MockEnvironment();
        env.setProperty("cas.property1", "value");
        env.setProperty("cas.property2", "https://github.com");
        env.setProperty("cas.property3", "classpath:/x509.crt");

        val condition = BeanCondition.on("cas.property1")
            .and("cas.property2").isUrl()
            .and("cas.property3").exists()
            .given(env)
            .get();
        assertTrue(condition);
    }

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
