/*
 * XML Type:  ProxiesType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ProxiesType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas.impl;
/**
 * An XML ProxiesType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public class ProxiesTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements edu.yale.tp.cas.ProxiesType
{
    
    public ProxiesTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PROXY$0 = 
        new javax.xml.namespace.QName("http://www.yale.edu/tp/cas", "proxy");
    
    
    /**
     * Gets array of all "proxy" elements
     */
    public java.lang.String[] getProxyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PROXY$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "proxy" element
     */
    public java.lang.String getProxyArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROXY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "proxy" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetProxyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PROXY$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "proxy" element
     */
    public org.apache.xmlbeans.XmlString xgetProxyArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PROXY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "proxy" element
     */
    public int sizeOfProxyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROXY$0);
        }
    }
    
    /**
     * Sets array of all "proxy" element
     */
    public void setProxyArray(java.lang.String[] proxyArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(proxyArray, PROXY$0);
        }
    }
    
    /**
     * Sets ith "proxy" element
     */
    public void setProxyArray(int i, java.lang.String proxy)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROXY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(proxy);
        }
    }
    
    /**
     * Sets (as xml) array of all "proxy" element
     */
    public void xsetProxyArray(org.apache.xmlbeans.XmlString[]proxyArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(proxyArray, PROXY$0);
        }
    }
    
    /**
     * Sets (as xml) ith "proxy" element
     */
    public void xsetProxyArray(int i, org.apache.xmlbeans.XmlString proxy)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PROXY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(proxy);
        }
    }
    
    /**
     * Inserts the value as the ith "proxy" element
     */
    public void insertProxy(int i, java.lang.String proxy)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(PROXY$0, i);
            target.setStringValue(proxy);
        }
    }
    
    /**
     * Appends the value as the last "proxy" element
     */
    public void addProxy(java.lang.String proxy)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PROXY$0);
            target.setStringValue(proxy);
        }
    }
    
    /**
     * Removes the ith "proxy" element
     */
    public void removeProxy(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROXY$0, i);
        }
    }
}
