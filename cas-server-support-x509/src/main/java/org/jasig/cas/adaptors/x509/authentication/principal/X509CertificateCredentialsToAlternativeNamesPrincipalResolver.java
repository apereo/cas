/*
 * Copyright 2012 Tobias Schlemmer.
 * based on X509CertificateCredentialsToIdentifierPrincipalResolver,
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 * 
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.lang.Integer;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateParsingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import edu.vt.middleware.crypt.x509.DNUtils;
import edu.vt.middleware.crypt.x509.types.AttributeType;

import org.jasig.cas.adaptors.x509.authentication.principal.AbstractX509CertificateCredentialsToPrincipalResolver;


/**
 * Credential to principal resolver that extracts one or more attribute values
 * from the certificate subject Alternative Names and combines them with intervening delimiters.
 * 
 * <p>
 * This class is based on source code from X509CertificateCredentialsToSubjectPrinciplalResolver.
 *
 * @author Tobias Schlemmer
 * @version $Revision$ $Date$
 * @since 
 *
 */
public class X509CertificateCredentialsToAlternativeNamesPrincipalResolver
    extends AbstractX509CertificateCredentialsToPrincipalResolver {
   
    /** Pattern used to extract attribute names from descriptor */
    private static final Pattern ATTR_PATTERN = Pattern.compile("\\$(\\w+)");
    
    private static final int ATTR_OTHERNAME     = 0;
    private static final int ATTR_RFC822NAME    = 1;
    private static final int ATTR_DNSNAME       = 2;
    private static final int ATTR_X400ADDRESS   = 3;
    private static final int ATTR_DIRECTORYNAME = 4;
    private static final int ATTR_EDIPARTYNAME  = 5;
    private static final int ATTR_URI           = 6;
    private static final int ATTR_IPADDRESS     = 7;
    private static final int ATTR_REGISTEREDID  = 8;

    
    /** Descriptor representing an abstract format of the principal to be resolved.*/
    @NotNull
    private String descriptor;

    /**
     * Sets the descriptor that describes for format of the principal ID to
     * create from X.509 AlternativeNames attributes.  The descriptor is made up of
     * common X.509 attribute names prefixed by "$", which are replaced by
     * attribute values extracted from SubjectAlternativeName attribute values.
     * <p>
     * EXAMPLE:
     * <p>
     * <pre>
     * <bean class="org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentialsToSubjectPrincipalResolver"
     *   p:descriptor="$RFC822Name" />
     * </pre>
     * <p>
     * The above bean when applied to a certificate with 
     * produces a principal of the form <strong>user@machine.example.com</strong>.
     *
     * @param s Descriptor string where attribute names are prefixed with "$"
     * to identify replacement by real attribute values from the subject DN.
     * Valid attributes include common X.509 DN attributes such as the following:
     * <ul>
     * <li>OTHERNAME</li>
     * <li>RFC822NAME</li>
     * <li>DNSNAME</li>
     * <li>X400ADDRESS</li>
     * <li>DIRECTORYNAME</li>
     * <li>EDIPARTYNAME</li>
     * <li>URI</li>
     * <li>IPADDRESS</li>
     * <li>REGISTEREDIDgg</li>
     * </ul>
     * 
     */
    public void setDescriptor(final String s) {
        this.descriptor = s;
    }

    /**
     * Replaces placeholders in the descriptor with values extracted from attribute
     * values in relative distinguished name components of the DN.
     * 
     * @param certificate X.509 certificate credential.
     * @return Resolved principal ID.
     * @see org.jasig.cas.adaptors.x509.authentication.principal.AbstractX509CertificateCredentialsToPrincipalResolver#resolvePrincipalInternal(java.security.cert.X509Certificate)
     */
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        this.log.debug("Resolving principal \"" + this.descriptor + "\" for " + certificate);
        final StringBuffer sb = new StringBuffer ();
        final Matcher m = ATTR_PATTERN.matcher(this.descriptor);
        final Map<String, AttributeContext> attrMap = new HashMap<String, AttributeContext>();
	//	Map<String,int> 
        String name = "";
        String[] values;
        AttributeContext context;
	try {
	    this.log.debug("certificate.getSubjectAlternativeNames() = " 
			   + certificate.getSubjectAlternativeNames());
	    Collection<List<?>> subjAltNames = certificate.getSubjectAlternativeNames();
	    this.log.debug("subjAltNmaes = " + subjAltNames);

	    if (subjAltNames != null) {
		for ( List<?> next : subjAltNames) {
		    
		    switch (((Integer)next.get(0)).intValue())
		    {
		    case ATTR_OTHERNAME: 
			name = "OTHERNAME";
			break;
		    case ATTR_RFC822NAME:
			name = "RFC822NAME";
			break;
		    case ATTR_DNSNAME:
			name = "DNSNAME";
			break;
		    case ATTR_X400ADDRESS:
			name = "X400ADDRESS";
			break;
		    case ATTR_DIRECTORYNAME:
			name = "DIRECTORYNAME";
			break;
		    case ATTR_EDIPARTYNAME:
			name = "EDIPARTYNAME";
			break;
		    case ATTR_URI:
			name = "URI";
			break;
		    case ATTR_IPADDRESS:
			name = "IPADDRESS";
			break;
		    case ATTR_REGISTEREDID:
			name = "REGISTEREDID";
			break;
		    default:
			this.log.error("Unknown alternative name No. " + (Integer)next.get(0) 
				       + " in certificate" + certificate );
		    }

		    if (!attrMap.containsKey(name)) {
			values = new String[]{ (String) next.get(1) };
			context = new AttributeContext(name, values);
		    } else {
			context = attrMap.get(name);
			context.addValue((String) next.get(1));
		    }

		    attrMap.put(name,context);
		}
	    }

	    this.log.debug("Replacement map has been intitialized: " + attrMap);

	    while (m.find()) {
		name = m.group(1);
		this.log.debug("Searching for \"" + name + "\" in " + attrMap );
		if (!attrMap.containsKey(name)) {
		    m.appendReplacement(sb, "");
		} else {
		    context = attrMap.get(name);
		    m.appendReplacement(sb,context.nextValue());
		}
	    }

	    m.appendTail(sb);

	} catch (CertificateParsingException e) {
	    this.log.error("Error parsing Certificate: " + e); 
	    // @todo: check whether the debug level is appropriate
	}
        return sb.toString();
    }
    
    private static final class AttributeContext
    {
        private int currentIndex;
        private String name;
        private String[] values;
        
        public AttributeContext(final String name, final String[] values) {
            this.values = values;
        }
        
        public String nextValue() {
            if (this.currentIndex == this.values.length) {
                throw new IllegalStateException("No values remaining for attribute " + this.name);
            }
            return this.values[this.currentIndex++];
        }

	public void addValue(String value) {
	    int oldSize = values.length;
	    String[] newValues = new String[oldSize+1];
	    System.arraycopy (values,0,newValues,0,oldSize);
	    newValues[oldSize+1] = value;
	    values = newValues;
	}
    }
}
