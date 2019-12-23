package org.apereo.cas.web.flow;

import org.apereo.cas.config.MultiphaseAuthenticationConfiguration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultiphaseAuthenticationWebflowConfigurerTests}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Import({
    ThymeleafAutoConfiguration.class,
