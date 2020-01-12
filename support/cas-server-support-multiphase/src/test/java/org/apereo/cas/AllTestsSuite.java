package org.apereo.cas;

import org.apereo.cas.web.flow.PrepareForMultiphaseAuthenticationActionTests;
import org.apereo.cas.web.flow.MultiphaseAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.StoreUserIdForAuthenticationActionTests;


// TODO

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@SelectClasses({
	PrepareForMultiphaseAuthenticationActionTests.class,
	MultiphaseAuthenticationWebflowConfigurerTests.class,
	StoreUserIdForAuthenticationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {}
