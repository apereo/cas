// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import javax.xml.bind.annotation.*;

/**
 * The generated SOAP class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="loginSearchReturn" type="{http://console.inwebo.com}LoginSearchResult"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "loginSearchReturn"
})
@XmlRootElement(name = "loginSearchResponse")
public class LoginSearchResponse {

    @XmlElement(required = true)
    protected LoginSearchResult loginSearchReturn;

    /**
     * Obtient la valeur de la propriété loginSearchReturn.
     * 
     * @return
     *     possible object is
     *     {@link LoginSearchResult }
     *     
     */
    public LoginSearchResult getLoginSearchReturn() {
        return loginSearchReturn;
    }

    /**
     * Définit la valeur de la propriété loginSearchReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginSearchResult }
     *     
     */
    public void setLoginSearchReturn(LoginSearchResult value) {
        this.loginSearchReturn = value;
    }

}
