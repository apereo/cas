package org.jasig.cas.web.bind;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.principal.Credentials;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface CredentialsBinder {
	void bind(HttpServletRequest request, Credentials credentials);
}
