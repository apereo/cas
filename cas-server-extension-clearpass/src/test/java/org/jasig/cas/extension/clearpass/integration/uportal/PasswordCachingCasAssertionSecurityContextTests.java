/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.extension.clearpass.integration.uportal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.extension.clearpass.integration.uportal.PasswordCachingCasAssertionSecurityContext;
import org.junit.Test;
import org.mockito.Matchers;

/**
 * Created by IntelliJ IDEA.
 * User: battags
 * Date: 3/27/11
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class PasswordCachingCasAssertionSecurityContextTests {

	@Test
	public void testCasAssertion() {
		final MockPasswordCachingCasAssertionSecurityContext context = new MockPasswordCachingCasAssertionSecurityContext(
				"http://localhost:8080/test", "<cas:clearPassResponse xmlns:cas='http://www.yale.edu/tp/cas'>\n" + "<cas:clearPassSuccess> \n"
						+ "<cas:credentials>XXX&amp;XXXX</cas:credentials> \n" + "</cas:clearPassSuccess> \n" + "</cas:clearPassResponse>");
		final AttributePrincipal attributePrincipal = mock(AttributePrincipal.class);
		when(attributePrincipal.getName()).thenReturn("test");
		when(attributePrincipal.getProxyTicketFor(Matchers.<String> any())).thenReturn("PT-158-74");
		final Assertion assertion = new AssertionImpl(attributePrincipal);

		try {
			AssertionHolder.setAssertion(assertion);
			context.authenticate();
		} finally {
			AssertionHolder.clear();
		}
	}

	private class MockPasswordCachingCasAssertionSecurityContext extends PasswordCachingCasAssertionSecurityContext {

		private static final long	serialVersionUID	= 7675304632076185424L;
		private final String		response;

		public MockPasswordCachingCasAssertionSecurityContext(final String url, final String response) {
			super(url);
			this.response = response;
		}

		@Override
		protected String retrieveResponseFromServer(String url, String encoding) {
			return response;
		}
	}
}
