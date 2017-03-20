package org.apereo.cas.configuration.model.core.authentication;

/**
 * Configuration properties class for cas.principal.resolver.persondir.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class PersonDirPrincipalResolverProperties {

    private String principalAttribute;

    private boolean returnNull;
    private boolean principalResolutionFailureFatal;

    private int radix;
    private boolean hexZeroPadding;

    public boolean isPrincipalResolutionFailureFatal() {
        return principalResolutionFailureFatal;
    }

    public void setPrincipalResolutionFailureFatal(final boolean principalResolutionFailureFatal) {
        this.principalResolutionFailureFatal = principalResolutionFailureFatal;
    }

    public String getPrincipalAttribute() {
        return principalAttribute;
    }

    public void setPrincipalAttribute(final String principalAttribute) {
        this.principalAttribute = principalAttribute;
    }

    public boolean isReturnNull() {
        return returnNull;
    }

    public void setReturnNull(final boolean returnNull) {
        this.returnNull = returnNull;
    }

    public int getRadix() {
        return radix;
    }

    public void setRadix(int radix) {
        this.radix = radix;
    }

    public boolean isHexZeroPadding() {
        return hexZeroPadding;
    }

    public void setHexZeroPadding(boolean hexZeroPadding) {
        this.hexZeroPadding = hexZeroPadding;
    }
}
