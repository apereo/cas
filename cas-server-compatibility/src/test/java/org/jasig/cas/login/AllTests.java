
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

package org.jasig.cas.login;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of compatibility tests.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("CAS 2 compatibility tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ValidateCompatibilityTests.class);
		suite.addTestSuite(LoginAsCredentialsAcceptorCompatibilityTests.class);
		suite.addTestSuite(ProxyValidateCompatibilityTests.class);
		suite.addTestSuite(LogoutCompatibilityTests.class);
		suite.addTestSuite(LoginAsCredentialsRequestorCompatibilityTests.class);
		suite.addTestSuite(ServiceValidateCompatibilityTests.class);
		//$JUnit-END$
		return suite;
	}
}
