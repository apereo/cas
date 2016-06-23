package org.jasig.cas.web.wavity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * This class is used for supporting the customized version of wavity CAS UI
 * especially in the scriptlet in JSP views.
 * 
 * @author davidlee
 *
 */
public final class ThemeUtils {
    
	/**
	 * JPEG Phto directory.
	 */
	private static final String JPEG_PHOTO_PATH = "themes/wavity/res/lib/custom/img/LogInScreen";
	
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
    private static final Logger logger = LoggerFactory.getLogger(ThemeUtils.class);
    
    /**
     * ThemeUtils constructor
     * 
     * Nothing to do in the constructor for now
     */
    public ThemeUtils() {
    }
    
    /**
     * Fetches the tenant logo image URL
     * 
     * @param tenantName
     * @return JSON string | null
     */
    public static final String fetchTenantLogo(final HttpServletRequest request, final String tenantName) {
    	if (!hasJpegPhoto(request, tenantName)) {
    		fetchJpegPhotoFromLdap(request, tenantName, ENTRY_TYPE_TENANTS);
    	}
    	return getJpegPhotoFullPath(request, tenantName);
    }
    
    /**
     * Fetches the app logo image URL
     * 
     * @param appName
     * @return JSON string | null
     */
    public static final String fetchAppLogo(final HttpServletRequest request, final String appName) {
    	if (!hasJpegPhoto(request, appName)) {
    		fetchJpegPhotoFromLdap(request, appName, ENTRY_TYPE_SERVICE);
    	}
    	return getJpegPhotoFullPath(request, appName);
    }
    
    /**
     * Gets the full path of the JPEG photo.
     * 
     * @param request the object of http servlet request.
     * @param name the name of tenant or app.
     * @return the string of JPEG photo full path.
     */
    private static final String getJpegPhotoFullPath(final HttpServletRequest request, final String name) {
    	final String contextPath = request.getContextPath();
		final String fullPath = String.format("%s/%s/%s.jpeg", contextPath, JPEG_PHOTO_PATH, name.toLowerCase());
		// In case there's no jpegPhoto in LDAP entry, can't get the image even if we searched it.
		if (!hasJpegPhoto(request, name)) {
			return null;
		}
		return fullPath;
    }
    
    /**
     * Gets the real path in the file system of the JPEG photo.
     * 
     * @param request the object of http servlet request.
     * @param name the name of tenant or app.
     * @return the string of JPEG photo full path.
     */
    private static final String getJpegPhotoRealPath(final HttpServletRequest request, final String name) {
    	final String realPath = request.getServletContext().getRealPath("/");
    	final String fullPath = String.format("%s%s/%s.jpeg", realPath, JPEG_PHOTO_PATH, name.toLowerCase());
    	return fullPath;
    }
    
    /**
     * Checks if there is downloaded JPEG photo file.
     * 
     * @param request the object of http servlet request.
     * @param name the name of app or tenant.
     * @return whether the photo file exists or not.
     */
	private static final boolean hasJpegPhoto(final HttpServletRequest request, final String name) {
		final File file = new File(getJpegPhotoRealPath(request, name));
		return file.exists();
	}
    
	/**
	 * Fetches the JPEG photo from LDAP.
	 * 
	 * @param request the object of http servlet request.
	 * @param name the name of tenant or app.
	 * @param type the type of LDAP entry.
	 */
	private static final void fetchJpegPhotoFromLdap(final HttpServletRequest request, final String name, final String type) {
		// Set up environment for creating initial context
		final Hashtable<String, String> env = new Hashtable<String, String>(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://wavitydevelopmentldap:389");
		env.put("java.naming.ldap.attributes.binary", "jpegPhoto");
		
		// Create initial context
		final LdapContext ctx;
		
		// Set the LDAP search base as tenants by default
		final String ldapSearchBase;
		
		// Change the LDAP search base considering the entry type. 
		if (type.equals(ENTRY_TYPE_TENANTS)) {
			ldapSearchBase = String.format("cn=%s,o=tenants,dc=wavity,dc=com", name.toLowerCase());
		} else if (type.equals(ENTRY_TYPE_SERVICE)) {
			ldapSearchBase = String.format("wavityCloudServiceName=%s,o=Cloud Services,dc=wavity,dc=com", name.toLowerCase());
		} else {
			ldapSearchBase = String.format("cn=%s,o=tenants,dc=wavity,dc=com", name.toLowerCase());
		}
		final String searchFilter = "jpegPhoto=*";
		final SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
		
		try {
			ctx = new InitialLdapContext(env, null);
			final NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
			SearchResult searchResult = null;
			if (results.hasMoreElements()) {
				searchResult = results.nextElement();
			}
			if (searchResult == null) {
				logger.error("No search result found");
				return;
			}
			// Get all attributes
			final Attributes attrs = searchResult.getAttributes();
			
			// See what we got
			final Attribute attr = attrs.get("jpegPhoto");
			if (attr != null) {
				final byte[] jpegByte = (byte[]) attr.get();
				final String jpegPhotoRealPath = getJpegPhotoRealPath(request, name);
				final FileOutputStream fileOut = new FileOutputStream(jpegPhotoRealPath);
				fileOut.write(jpegByte);
				fileOut.close();
			}
			// Close the context when we're done
			ctx.close();
		} catch (NamingException e) {
			logger.warn("An exception happened!", e);
		} catch (FileNotFoundException e) {
			logger.warn("An exception happened!", e);
		} catch (IOException e) {
			logger.warn("An exception happened!", e);
		} finally {
		}
	}
        
}
