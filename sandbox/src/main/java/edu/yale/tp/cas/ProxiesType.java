/*
 * XML Type:  ProxiesType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ProxiesType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas;


/**
 * An XML ProxiesType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public interface ProxiesType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.sB9D3B5FB07C1B153996203ECA5FC9DDC.TypeSystemHolder.typeSystem.resolveHandle("proxiestypec982type");
    
    /**
     * Gets array of all "proxy" elements
     */
    java.lang.String[] getProxyArray();
    
    /**
     * Gets ith "proxy" element
     */
    java.lang.String getProxyArray(int i);
    
    /**
     * Gets (as xml) array of all "proxy" elements
     */
    org.apache.xmlbeans.XmlString[] xgetProxyArray();
    
    /**
     * Gets (as xml) ith "proxy" element
     */
    org.apache.xmlbeans.XmlString xgetProxyArray(int i);
    
    /**
     * Returns number of "proxy" element
     */
    int sizeOfProxyArray();
    
    /**
     * Sets array of all "proxy" element
     */
    void setProxyArray(java.lang.String[] proxyArray);
    
    /**
     * Sets ith "proxy" element
     */
    void setProxyArray(int i, java.lang.String proxy);
    
    /**
     * Sets (as xml) array of all "proxy" element
     */
    void xsetProxyArray(org.apache.xmlbeans.XmlString[]proxyArray);
    
    /**
     * Sets (as xml) ith "proxy" element
     */
    void xsetProxyArray(int i, org.apache.xmlbeans.XmlString proxy);
    
    /**
     * Inserts the value as the ith "proxy" element
     */
    void insertProxy(int i, java.lang.String proxy);
    
    /**
     * Appends the value as the last "proxy" element
     */
    void addProxy(java.lang.String proxy);
    
    /**
     * Removes the ith "proxy" element
     */
    void removeProxy(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static edu.yale.tp.cas.ProxiesType newInstance() {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        public static edu.yale.tp.cas.ProxiesType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.ProxiesType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
