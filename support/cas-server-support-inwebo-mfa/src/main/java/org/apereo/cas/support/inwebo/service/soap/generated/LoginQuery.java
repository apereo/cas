// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "userid",
    "loginid"
})
@XmlRootElement(name = "loginQuery")
public class LoginQuery {

    protected long userid;
    protected long loginid;

    /**
     * Obtient la valeur de la propriété userid.
     * 
     */
    public long getUserid() {
        return userid;
    }

    /**
     * Définit la valeur de la propriété userid.
     * 
     */
    public void setUserid(long value) {
        this.userid = value;
    }

    /**
     * Obtient la valeur de la propriété loginid.
     * 
     */
    public long getLoginid() {
        return loginid;
    }

    /**
     * Définit la valeur de la propriété loginid.
     * 
     */
    public void setLoginid(long value) {
        this.loginid = value;
    }

}
