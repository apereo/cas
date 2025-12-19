package org.apereo.cas.support.inwebo.service.soap.generated;

import module java.base;
import org.apache.commons.lang3.StringUtils;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = StringUtils.EMPTY, propOrder = {
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
    public void setUserid(final long value) {
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
    public void setLoginid(final long value) {
        this.loginid = value;
    }

}
