/*
 * XML Type:  ServiceResponseType
 * Namespace: http://www.yale.edu/tp/cas
 * Java type: edu.yale.tp.cas.ServiceResponseType
 *
 * Automatically generated - do not modify.
 */
package edu.yale.tp.cas;


/**
 * An XML ServiceResponseType(@http://www.yale.edu/tp/cas).
 *
 * This is a complex type.
 */
public interface ServiceResponseType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.sB9D3B5FB07C1B153996203ECA5FC9DDC.TypeSystemHolder.typeSystem.resolveHandle("serviceresponsetype1cb8type");
    
    /**
     * Gets the "authenticationSuccess" element
     */
    edu.yale.tp.cas.AuthenticationSuccessType getAuthenticationSuccess();
    
    /**
     * True if has "authenticationSuccess" element
     */
    boolean isSetAuthenticationSuccess();
    
    /**
     * Sets the "authenticationSuccess" element
     */
    void setAuthenticationSuccess(edu.yale.tp.cas.AuthenticationSuccessType authenticationSuccess);
    
    /**
     * Appends and returns a new empty "authenticationSuccess" element
     */
    edu.yale.tp.cas.AuthenticationSuccessType addNewAuthenticationSuccess();
    
    /**
     * Unsets the "authenticationSuccess" element
     */
    void unsetAuthenticationSuccess();
    
    /**
     * Gets the "authenticationFailure" element
     */
    edu.yale.tp.cas.AuthenticationFailureType getAuthenticationFailure();
    
    /**
     * True if has "authenticationFailure" element
     */
    boolean isSetAuthenticationFailure();
    
    /**
     * Sets the "authenticationFailure" element
     */
    void setAuthenticationFailure(edu.yale.tp.cas.AuthenticationFailureType authenticationFailure);
    
    /**
     * Appends and returns a new empty "authenticationFailure" element
     */
    edu.yale.tp.cas.AuthenticationFailureType addNewAuthenticationFailure();
    
    /**
     * Unsets the "authenticationFailure" element
     */
    void unsetAuthenticationFailure();
    
    /**
     * Gets the "proxySuccess" element
     */
    edu.yale.tp.cas.ProxySuccessType getProxySuccess();
    
    /**
     * True if has "proxySuccess" element
     */
    boolean isSetProxySuccess();
    
    /**
     * Sets the "proxySuccess" element
     */
    void setProxySuccess(edu.yale.tp.cas.ProxySuccessType proxySuccess);
    
    /**
     * Appends and returns a new empty "proxySuccess" element
     */
    edu.yale.tp.cas.ProxySuccessType addNewProxySuccess();
    
    /**
     * Unsets the "proxySuccess" element
     */
    void unsetProxySuccess();
    
    /**
     * Gets the "proxyFailure" element
     */
    edu.yale.tp.cas.ProxyFailureType getProxyFailure();
    
    /**
     * True if has "proxyFailure" element
     */
    boolean isSetProxyFailure();
    
    /**
     * Sets the "proxyFailure" element
     */
    void setProxyFailure(edu.yale.tp.cas.ProxyFailureType proxyFailure);
    
    /**
     * Appends and returns a new empty "proxyFailure" element
     */
    edu.yale.tp.cas.ProxyFailureType addNewProxyFailure();
    
    /**
     * Unsets the "proxyFailure" element
     */
    void unsetProxyFailure();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static edu.yale.tp.cas.ServiceResponseType newInstance() {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        public static edu.yale.tp.cas.ServiceResponseType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (edu.yale.tp.cas.ServiceResponseType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
