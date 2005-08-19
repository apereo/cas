/*
 * XML Type:  ServiceResponseType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ServiceResponseType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas.impl;
/**
 * An XML ServiceResponseType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public class ServiceResponseTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements edu.yale.tp.cas.ServiceResponseType
{
    
    public ServiceResponseTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AUTHENTICATIONSUCCESS$0 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "authenticationSuccess");
    private static final javax.xml.namespace.QName AUTHENTICATIONFAILURE$2 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "authenticationFailure");
    private static final javax.xml.namespace.QName PROXYSUCCESS$4 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "proxySuccess");
    private static final javax.xml.namespace.QName PROXYFAILURE$6 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "proxyFailure");
    
    
    /**
     * Gets the "authenticationSuccess" element
     */
    public edu.yale.tp.cas.AuthenticationSuccessType getAuthenticationSuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.AuthenticationSuccessType target = null;
            target = (edu.yale.tp.cas.AuthenticationSuccessType)get_store().find_element_user(AUTHENTICATIONSUCCESS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "authenticationSuccess" element
     */
    public boolean isSetAuthenticationSuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AUTHENTICATIONSUCCESS$0) != 0;
        }
    }
    
    /**
     * Sets the "authenticationSuccess" element
     */
    public void setAuthenticationSuccess(edu.yale.tp.cas.AuthenticationSuccessType authenticationSuccess)
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.AuthenticationSuccessType target = null;
            target = (edu.yale.tp.cas.AuthenticationSuccessType)get_store().find_element_user(AUTHENTICATIONSUCCESS$0, 0);
            if (target == null)
            {
                target = (edu.yale.tp.cas.AuthenticationSuccessType)get_store().add_element_user(AUTHENTICATIONSUCCESS$0);
            }
            target.set(authenticationSuccess);
        }
    }
    
    /**
     * Appends and returns a new empty "authenticationSuccess" element
     */
    public edu.yale.tp.cas.AuthenticationSuccessType addNewAuthenticationSuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.AuthenticationSuccessType target = null;
            target = (edu.yale.tp.cas.AuthenticationSuccessType)get_store().add_element_user(AUTHENTICATIONSUCCESS$0);
            return target;
        }
    }
    
    /**
     * Unsets the "authenticationSuccess" element
     */
    public void unsetAuthenticationSuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AUTHENTICATIONSUCCESS$0, 0);
        }
    }
    
    /**
     * Gets the "authenticationFailure" element
     */
    public edu.yale.tp.cas.AuthenticationFailureType getAuthenticationFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.AuthenticationFailureType target = null;
            target = (edu.yale.tp.cas.AuthenticationFailureType)get_store().find_element_user(AUTHENTICATIONFAILURE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "authenticationFailure" element
     */
    public boolean isSetAuthenticationFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AUTHENTICATIONFAILURE$2) != 0;
        }
    }
    
    /**
     * Sets the "authenticationFailure" element
     */
    public void setAuthenticationFailure(edu.yale.tp.cas.AuthenticationFailureType authenticationFailure)
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.AuthenticationFailureType target = null;
            target = (edu.yale.tp.cas.AuthenticationFailureType)get_store().find_element_user(AUTHENTICATIONFAILURE$2, 0);
            if (target == null)
            {
                target = (edu.yale.tp.cas.AuthenticationFailureType)get_store().add_element_user(AUTHENTICATIONFAILURE$2);
            }
            target.set(authenticationFailure);
        }
    }
    
    /**
     * Appends and returns a new empty "authenticationFailure" element
     */
    public edu.yale.tp.cas.AuthenticationFailureType addNewAuthenticationFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.AuthenticationFailureType target = null;
            target = (edu.yale.tp.cas.AuthenticationFailureType)get_store().add_element_user(AUTHENTICATIONFAILURE$2);
            return target;
        }
    }
    
    /**
     * Unsets the "authenticationFailure" element
     */
    public void unsetAuthenticationFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AUTHENTICATIONFAILURE$2, 0);
        }
    }
    
    /**
     * Gets the "proxySuccess" element
     */
    public edu.yale.tp.cas.ProxySuccessType getProxySuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxySuccessType target = null;
            target = (edu.yale.tp.cas.ProxySuccessType)get_store().find_element_user(PROXYSUCCESS$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "proxySuccess" element
     */
    public boolean isSetProxySuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROXYSUCCESS$4) != 0;
        }
    }
    
    /**
     * Sets the "proxySuccess" element
     */
    public void setProxySuccess(edu.yale.tp.cas.ProxySuccessType proxySuccess)
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxySuccessType target = null;
            target = (edu.yale.tp.cas.ProxySuccessType)get_store().find_element_user(PROXYSUCCESS$4, 0);
            if (target == null)
            {
                target = (edu.yale.tp.cas.ProxySuccessType)get_store().add_element_user(PROXYSUCCESS$4);
            }
            target.set(proxySuccess);
        }
    }
    
    /**
     * Appends and returns a new empty "proxySuccess" element
     */
    public edu.yale.tp.cas.ProxySuccessType addNewProxySuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxySuccessType target = null;
            target = (edu.yale.tp.cas.ProxySuccessType)get_store().add_element_user(PROXYSUCCESS$4);
            return target;
        }
    }
    
    /**
     * Unsets the "proxySuccess" element
     */
    public void unsetProxySuccess()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROXYSUCCESS$4, 0);
        }
    }
    
    /**
     * Gets the "proxyFailure" element
     */
    public edu.yale.tp.cas.ProxyFailureType getProxyFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxyFailureType target = null;
            target = (edu.yale.tp.cas.ProxyFailureType)get_store().find_element_user(PROXYFAILURE$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "proxyFailure" element
     */
    public boolean isSetProxyFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROXYFAILURE$6) != 0;
        }
    }
    
    /**
     * Sets the "proxyFailure" element
     */
    public void setProxyFailure(edu.yale.tp.cas.ProxyFailureType proxyFailure)
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxyFailureType target = null;
            target = (edu.yale.tp.cas.ProxyFailureType)get_store().find_element_user(PROXYFAILURE$6, 0);
            if (target == null)
            {
                target = (edu.yale.tp.cas.ProxyFailureType)get_store().add_element_user(PROXYFAILURE$6);
            }
            target.set(proxyFailure);
        }
    }
    
    /**
     * Appends and returns a new empty "proxyFailure" element
     */
    public edu.yale.tp.cas.ProxyFailureType addNewProxyFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            edu.yale.tp.cas.ProxyFailureType target = null;
            target = (edu.yale.tp.cas.ProxyFailureType)get_store().add_element_user(PROXYFAILURE$6);
            return target;
        }
    }
    
    /**
     * Unsets the "proxyFailure" element
     */
    public void unsetProxyFailure()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROXYFAILURE$6, 0);
        }
    }
}
