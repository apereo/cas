package org.apereo.cas.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Response;
import org.ldaptive.auth.AuthenticationCriteria;
import org.ldaptive.auth.AuthenticationHandlerResponse;
import org.ldaptive.auth.PooledCompareAuthenticationHandler;
import org.ldaptive.pool.PooledConnectionFactory;

public class PooledCompareCustomAttributeAuthenticationHandler extends
		PooledCompareAuthenticationHandler {

	protected static final String DEFAULT_PASSWORD_ATTRIBUTE_NAME = "userPassword";

	private String passwordAttributeName = DEFAULT_PASSWORD_ATTRIBUTE_NAME;
	
	public PooledCompareCustomAttributeAuthenticationHandler(PooledConnectionFactory cf) {
		super(cf);
	}

	public String getPasswordAttributeName() {
		return passwordAttributeName;
	}

	public void setPasswordAttributeName(String passwordAttributeName) {
		this.passwordAttributeName = passwordAttributeName;
	}

	/** {@inheritDoc} */
	@Override
	  protected AuthenticationHandlerResponse authenticateInternal(
	    final Connection c,
	    final AuthenticationCriteria criteria)
	    throws LdapException
	  {
	    byte[] hash;
	    try {
	      final MessageDigest md = MessageDigest.getInstance(getPasswordScheme());
	      md.update(criteria.getCredential().getBytes());
	      hash = md.digest();
	    } catch (NoSuchAlgorithmException e) {
	      throw new LdapException(e);
	    }

	    final LdapAttribute la = new LdapAttribute(
	    		getPasswordAttributeName(),
	      String.format("{%s}%s", getPasswordScheme(), LdapUtils.base64Encode(hash)).getBytes());
	    final CompareOperation compare = new CompareOperation(c);
	    final CompareRequest request = new CompareRequest(criteria.getDn(), la);
	    request.setControls(getAuthenticationControls());

	    final Response<Boolean> compareResponse = compare.execute(request);
	    return
	      new AuthenticationHandlerResponse(
	        compareResponse.getResult(),
	        compareResponse.getResultCode(),
	        c,
	        compareResponse.getMessage(),
	        compareResponse.getControls(),
	        compareResponse.getMessageId());
	  }
}