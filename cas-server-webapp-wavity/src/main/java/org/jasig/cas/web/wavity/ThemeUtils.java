
package org.jasig.cas.web.wavity;

import java.util.Base64;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for supporting the customized version of WAVITY CAS UI.
 *
 * @author davidlee
 *
 */
public final class ThemeUtils {
	/**
	 * Tenants API suffix
	 */
	private static final String ENTRY_TYPE_TENANTS = "Tenants";

	/**
	 * Service API suffix
	 */
	private static final String ENTRY_TYPE_SERVICE = "CloudServices";

	/**
	 * logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ThemeUtils.class);

	/**
	 * ThemeUtils constructor
	 *
	 * Nothing to do in the constructor for now
	 */
	public ThemeUtils() {
	}

	/**
	 * Fetches the tenant LOGO.
	 *
	 * @param tenantName
	 * @return JSON string | null
	 */
	public static final String fetchTenantLogo(final HttpServletRequest request, final String tenantName) {
		return fetchJpegPhotoFromLdap(request, tenantName, ENTRY_TYPE_TENANTS);
	}

	/**
	 * Fetches the APP LOGO.
	 *
	 * @param appName
	 * @return JSON string | null
	 */
	public static final String fetchAppLogo(final HttpServletRequest request, final String appName) {
		return fetchJpegPhotoFromLdap(request, appName, ENTRY_TYPE_SERVICE);
	}

	/**
	 * Fetches the JPEG photo from LDAP.
	 *
	 * @param request
	 *            the object of HTTP SERVLET request.
	 * @param name
	 *            the name of Tenant or APP.
	 * @param type
	 *            the type of LDAP entry.
	 */
	private static final String fetchJpegPhotoFromLdap(final HttpServletRequest request, final String name,
			final String type) {
		final Hashtable<String, String> env = new Hashtable<String, String>(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://192.168.2.27:389");
		env.put("java.naming.ldap.attributes.binary", "jpegPhoto;portalBranding;binary");// 104
		LdapContext ctx = null;
		final String ldapSearchBase;
		if (type.equals(ENTRY_TYPE_TENANTS)) {
			ldapSearchBase = String.format("cn=%s,o=tenants,dc=wavity,dc=com", name.toLowerCase());
		} else if (type.equals(ENTRY_TYPE_SERVICE)) {
			ldapSearchBase = String.format("wavityCloudServiceName=%s,o=Cloud Services,dc=wavity,dc=com",
					name.toLowerCase());
		} else {
			ldapSearchBase = String.format("cn=%s,o=tenants,dc=wavity,dc=com", name.toLowerCase());
		}
		String searchFilter = "jpegPhoto;portalBranding;binary=*";
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
		try {
			ctx = new InitialLdapContext(env, null);

			SearchResult searchResult = searchImage(ctx, searchControls, ldapSearchBase, searchFilter);
			if (searchResult == null) {
				env.put("java.naming.ldap.attributes.binary", "jpegPhoto;primary;binary");
				ctx = new InitialLdapContext(env, null);
				searchFilter = "jpegPhoto;primary;binary=*";
				searchControls = new SearchControls();
				searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);

				searchResult = searchImage(ctx, searchControls, ldapSearchBase, searchFilter);
				if (searchResult == null) {
					return null;
				}

				LOG.info("Portal Branding image is not available .fetching the binary image");
				return buildImage(searchResult, "jpegPhoto;primary;binary");

			} else {
				LOG.info("portal branding image is availabel so fecting that image");
				return buildImage(searchResult, "jpegPhoto;portalBranding;binary");
			}

		} catch (final NamingException e) {
			LOG.warn("No search result found");
		} finally {
			try {
				ctx.close();
			} catch (final NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private static SearchResult searchImage(final LdapContext ctx, final SearchControls searchControls,
			final String ldapSearchBase, final String searchFilter) throws NamingException {

		final NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
		SearchResult searchResult = null;
		if (results.hasMoreElements()) {
			searchResult = results.nextElement();
		}
		if (searchResult == null) {
			return null;
		}
		return searchResult;
	}

	private static String buildImage(final SearchResult searchResult, final String filter) throws NamingException {
		final StringBuilder builder = new StringBuilder();
		final Attributes attrs = searchResult.getAttributes();
		final Attribute attr = attrs.get(filter);// 141
		if (attr != null) {
			final byte[] jpegByte = (byte[]) attr.get();
			final String base64EncodedJpegPhoto = Base64.getEncoder().encodeToString(jpegByte);

			builder.append("data:image/jpeg;base64,").append(base64EncodedJpegPhoto);

		}
		return builder.toString();

	}
}
