
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
public final class ThemeUtils
{
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
  private static final Logger LOG =
      LoggerFactory.getLogger(ThemeUtils.class);



  /**
   * ThemeUtils constructor
   *
   * Nothing to do in the constructor for now
   */
  public ThemeUtils()
  {
  }



  /**
   * Fetches the tenant LOGO.
   *
   * @param tenantName
   * @return JSON string | null
   */
  public static final String fetchTenantLogo(
      final HttpServletRequest request, final String tenantName)
  {
    return fetchJpegPhotoFromLdap(request, tenantName,
        ENTRY_TYPE_TENANTS);
  }



  /**
   * Fetches the APP LOGO.
   *
   * @param appName
   * @return JSON string | null
   */
  public static final String fetchAppLogo(
      final HttpServletRequest request, final String appName)
  {
    return fetchJpegPhotoFromLdap(request, appName,
        ENTRY_TYPE_SERVICE);
  }



  /**
   * Fetches the JPEG photo from LDAP.
   *
   * @param request the object of HTTP SERVLET request.
   * @param name the name of Tenant or APP.
   * @param type the type of LDAP entry.
   */
  private static final String fetchJpegPhotoFromLdap(
      final HttpServletRequest request, final String name,
      final String type)
  {
    final Hashtable<String, String> env =
        new Hashtable<String, String>(11);
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://wavitydevelopmentldap:389");
    env.put("java.naming.ldap.attributes.binary", "jpegPhoto;portalBranding;binary");
    final LdapContext ctx;
    final String ldapSearchBase;
    if (type.equals(ENTRY_TYPE_TENANTS))
    {
      ldapSearchBase = String.format(
          "cn=%s,o=tenants,dc=wavity,dc=com", name.toLowerCase());
    }
    else if (type.equals(ENTRY_TYPE_SERVICE))
    {
      ldapSearchBase = String.format(
          "wavityCloudServiceName=%s,o=Cloud Services,dc=wavity,dc=com",
          name.toLowerCase());
    }
    else
    {
      ldapSearchBase = String.format(
          "cn=%s,o=tenants,dc=wavity,dc=com", name.toLowerCase());
    }
    final String searchFilter = "jpegPhoto;portalBranding;binary=*";
    final SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
    try
    {
      ctx = new InitialLdapContext(env, null);
      final NamingEnumeration<SearchResult> results =
          ctx.search(ldapSearchBase, searchFilter, searchControls);
      SearchResult searchResult = null;
      if (results.hasMoreElements())
      {
        searchResult = results.nextElement();
      }
      if (searchResult == null)
      {
        return null;
      }
      final Attributes attrs = searchResult.getAttributes();
      final Attribute attr = attrs.get("jpegPhoto;portalBranding;binary");
      if (attr != null)
      {
        final byte[] jpegByte = (byte[]) attr.get();
        final String base64EncodedJpegPhoto =
            Base64.getEncoder().encodeToString(jpegByte);
        final StringBuilder builder = new StringBuilder();
        builder
            .append("data:image/jpeg;base64,")
            .append(base64EncodedJpegPhoto);
        return builder.toString();
      }
      ctx.close();
    }
    catch (final NamingException e)
    {
      LOG.warn("No search result found");
    }
    finally
    {
    }
    return null;
  }
}
