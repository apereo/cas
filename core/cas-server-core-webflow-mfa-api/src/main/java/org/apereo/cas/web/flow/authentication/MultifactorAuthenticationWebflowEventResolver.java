package org.apereo.cas.web.flow.authentication;

import module java.base;

/**
 * This is {@link MultifactorAuthenticationWebflowEventResolver}. Tags a bean definition method whose
 * webflow event resolver should be consulted by the initial authentication event resolver to find
 * multifactor authentication events. Tagged beans are collected from the application context and
 * added to the initial authentication event resolver in their declared order.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MultifactorAuthenticationWebflowEventResolver {
}
