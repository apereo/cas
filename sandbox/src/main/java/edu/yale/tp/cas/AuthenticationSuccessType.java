/*
 * XML Type:  AuthenticationSuccessType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.AuthenticationSuccessType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas;


/**
 * An XML AuthenticationSuccessType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public interface AuthenticationSuccessType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.sB9D3B5FB07C1B153996203ECA5FC9DDC.TypeSystemHolder.typeSystem.resolveHandle("authenticationsuccesstyped303type");
    
    /**
     * Gets the "user" element
     */
    java.lang.String getUser();
    
    /**
     * Gets (as xml) the "user" element
     */
    org.apache.xmlbeans.XmlString xgetUser();
    
    /**
     * Sets the "user" element
     */
    void setUser(java.lang.String user);
    
    /**
     * Sets (as xml) the "user" element
     */
    void xsetUser(org.apache.xmlbeans.XmlString user);
    
    /**
     * Gets the "proxyGrantingTicket" element
     */
    java.lang.String getProxyGrantingTicket();
    
    /**
     * Gets (as xml) the "proxyGrantingTicket" element
     */
    org.apache.xmlbeans.XmlString xgetProxyGrantingTicket();
    
    /**
     * True if has "proxyGrantingTicket" element
     */
    boolean isSetProxyGrantingTicket();
    
    /**
     * Sets the "proxyGrantingTicket" element
     */
    void setProxyGrantingTicket(java.lang.String proxyGrantingTicket);
    
    /**
     * Sets (as xml) the "proxyGrantingTicket" element
     */
    void xsetProxyGrantingTicket(org.apache.xmlbeans.XmlString proxyGrantingTicket);
    
    /**
     * Unsets the "proxyGrantingTicket" element
     */
    void unsetProxyGrantingTicket();
    
    /**
     * Gets the "proxies" element
     */
    edu.yale.tp.cas.ProxiesType getProxies();
    
    /**
     * True if has "proxies" element
     */
    boolean isSetProxies();
    
    /**
     * Sets the "proxies" element
     */
    void setProxies(edu.yale.tp.cas.ProxiesType proxies);
    
    /**
     * Appends and returns a new empty "proxies" element
     */
    edu.yale.tp.cas.ProxiesType addNewProxies();
    
    /**
     * Unsets the "proxies" element
     */
    void unsetProxies();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static edu.yale.tp.cas.AuthenticationSuccessType newInstance() {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        public static edu.yale.tp.cas.AuthenticationSuccessType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.AuthenticationSuccessType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
