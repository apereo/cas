
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
