// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import jakarta.xml.bind.annotation.*;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "loginQueryReturn"
})
@XmlRootElement(name = "loginQueryResponse")
public class LoginQueryResponse {

    @XmlElement(required = true)
    protected LoginQueryResult loginQueryReturn;

    /**
     * Obtient la valeur de la propriété loginQueryReturn.
     * 
     * @return
     *     possible object is
     *     {@link LoginQueryResult }
     *     
     */
    public LoginQueryResult getLoginQueryReturn() {
        return loginQueryReturn;
    }

    /**
     * Définit la valeur de la propriété loginQueryReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginQueryResult }
     *     
     */
    public void setLoginQueryReturn(LoginQueryResult value) {
        this.loginQueryReturn = value;
    }

}
