package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.cryptacular.x509.dn.Attribute;
import org.cryptacular.x509.dn.AttributeType;
import org.cryptacular.x509.dn.NameReader;
import org.cryptacular.x509.dn.RDN;
import org.cryptacular.x509.dn.RDNSequence;
import org.cryptacular.x509.dn.StandardAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Credential to principal resolver that extracts one or more attribute values
 * from the certificate subject DN and combines them with intervening delimiters.
 *
 * @author Marvin S. Addison
 * @since 3.4.4
 */
public class X509SubjectPrincipalResolver extends AbstractX509PrincipalResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(X509SubjectPrincipalResolver.class);

    /**
     * Pattern used to extract attribute names from descriptor.
     */
    private static final Pattern ATTR_PATTERN = Pattern.compile("\\$(\\w+)");

    /**
     * Descriptor representing an abstract format of the principal to be resolved.
     */
    private final String descriptor;

    /**
     * Sets the descriptor that describes for format of the principal ID to
     * create from X.509 subject DN attributes.  The descriptor is made up of
     * common X.509 attribute names prefixed by "$", which are replaced by
     * attribute values extracted from DN attribute values.
     * <p>
     * EXAMPLE:
     * </p>
     * {@code
     * {@code
     * <bean class="X509SubjectPrincipalResolver"
     * p:descriptor="$UID@$DC.$DC"
     * }
     * }*
     * <p>
     * The above bean when applied to a certificate with the DN
     * <p>
     * <b>DC=edu, DC=vt/UID=jacky, CN=Jascarnella Ellagwonto</b></p>
     * <p>
     * produces the principal <strong>jacky@vt.edu</strong>.</p>
     *
     * @param attributeRepository      the attribute repository
     * @param principalFactory         the principal factory
     * @param returnNullIfNoAttributes the return null if no attributes
     * @param principalAttributeName   the principal attribute name
     * @param descriptor               Descriptor string where attribute names are prefixed with "$"
     *                                 to identify replacement by real attribute values from the subject DN.
     *                                 Valid attributes include common X.509 DN attributes such as the following:
     *                                 <ul><li>C</li><li>CN</li><li>DC</li><li>EMAILADDRESS</li>
     *                                 <li>L</li><li>O</li><li>OU</li><li>SERIALNUMBER</li>
     *                                 <li>ST</li><li>UID</li><li>UNIQUEIDENTIFIER</li></ul>
     *                                 For a complete list of supported attributes, see {@link org.cryptacular.x509.dn.StandardAttributeType}.
     */
    public X509SubjectPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                        final boolean returnNullIfNoAttributes,
                                        final String principalAttributeName, final String descriptor) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
        this.descriptor = descriptor;
    }

    public X509SubjectPrincipalResolver(final String descriptor) {
        super();
        this.descriptor = descriptor;
    }

    /**
     * Replaces placeholders in the descriptor with values extracted from attribute
     * values in relative distinguished name components of the DN.
     *
     * @param certificate X.509 certificate credential.
     * @return Resolved principal ID.
     */
    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal for [{}]", certificate);
        final StringBuffer sb = new StringBuffer();
        final Matcher m = ATTR_PATTERN.matcher(this.descriptor);
        final Map<String, AttributeContext> attrMap = new HashMap<>();
        final RDNSequence rdnSequence = new NameReader(certificate).readSubject();
        String name;
        String[] values;
        AttributeContext context;
        while (m.find()) {
            name = m.group(1);
            if (!attrMap.containsKey(name)) {
                values = getAttributeValues(rdnSequence,
                        StandardAttributeType.fromName(name));
                attrMap.put(name, new AttributeContext(values));
            }
            context = attrMap.get(name);
            m.appendReplacement(sb, context.nextValue());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Gets the values of the given attribute contained in the DN.
     * <p>
     * <p><strong>NOTE:</strong> no escaping is done on special characters in the
     * values, which could be different from what would appear in the string
     * representation of the DN.</p>
     *
     * @param rdnSequence list of relative distinguished names
     *                    that contains the attributes comprising the DN.
     * @param attribute   Attribute whose values will be retrieved.
     * @return The attribute values for the given attribute in the order they
     * appear would appear in the string representation of the DN or an empty
     * array if the given attribute does not exist.
     */
    private static String[] getAttributeValues(final RDNSequence rdnSequence, final AttributeType attribute) {
        // Iterates sequence in reverse order as specified in section 2.1 of RFC 2253
        final List<String> values = new ArrayList<>();
        for (final RDN rdn : rdnSequence.backward()) {
            for (final Attribute attr : rdn.getAttributes()) {
                if (attr.getType().equals(attribute)) {
                    values.add(attr.getValue());
                }
            }
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("descriptor", descriptor)
                .toString();
    }


    private static class AttributeContext {
        private int currentIndex;
        private final String[] values;

        /**
         * Instantiates a new attribute context.
         *
         * @param values the values
         */
        AttributeContext(final String[] values) {
            this.values = values;
        }


        /**
         * Retrieve the next value, by incrementing the current index.
         *
         * @return the string
         * @throws IllegalStateException if no values are remaining.
         */
        public String nextValue() {
            if (this.currentIndex == this.values.length) {
                throw new IllegalStateException("No values remaining for attribute");
            }
            return this.values[this.currentIndex++];
        }
    }


}
