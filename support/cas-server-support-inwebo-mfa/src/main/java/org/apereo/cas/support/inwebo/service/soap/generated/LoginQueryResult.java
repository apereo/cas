package org.apereo.cas.support.inwebo.service.soap.generated;
import module java.base;
import lombok.Getter;
import lombok.Setter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginQueryResult", propOrder = {
    "err",
    "login",
    "code",
    "status",
    "role",
    "firstname",
    "name",
    "mail",
    "phone",
    "extrafields",
    "createdby",
    "lastauthdate",
    "nca",
    "caid",
    "castate",
    "caname",
    "cault",
    "caalias",
    "nma",
    "maid",
    "mastate",
    "maname",
    "maalias",
    "mapushenabled",
    "nmac",
    "macid",
    "macstate",
    "macname",
    "macalias",
    "macpushenabled",
    "nva",
    "vaid",
    "vastate",
    "vaname",
    "vaalias",
    "longcode"
})
@Getter
public class LoginQueryResult {

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String err;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String login;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String code;

    @Setter
    protected long status;

    @Setter
    protected long role;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String firstname;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String name;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String mail;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String phone;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String extrafields;

    @Setter
    protected long createdby;

    @Setter
    protected long lastauthdate;

    @Setter
    protected long nca;

    @XmlElement(required = true, nillable = true)
    protected List<Long> caid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> castate;

    @XmlElement(required = true, nillable = true)
    protected List<String> caname;

    @XmlElement(required = true, nillable = true)
    protected List<Long> cault;

    @XmlElement(required = true, nillable = true)
    protected List<String> caalias;

    @Setter
    protected long nma;

    @XmlElement(required = true, nillable = true)
    protected List<Long> maid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> mastate;

    @XmlElement(required = true, nillable = true)
    protected List<String> maname;

    @XmlElement(required = true, nillable = true)
    protected List<String> maalias;

    @XmlElement(required = true, nillable = true)
    protected List<Long> mapushenabled;

    @Setter
    protected long nmac;

    @XmlElement(required = true, nillable = true)
    protected List<Long> macid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> macstate;

    @XmlElement(required = true, nillable = true)
    protected List<String> macname;

    @XmlElement(required = true, nillable = true)
    protected List<String> macalias;

    @XmlElement(required = true, nillable = true)
    protected List<Long> macpushenabled;

    @Setter
    protected long nva;

    @XmlElement(required = true, nillable = true)
    protected List<Long> vaid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> vastate;

    @XmlElement(required = true, nillable = true)
    protected List<String> vaname;

    @XmlElement(required = true, nillable = true)
    protected List<String> vaalias;

    @Setter
    @XmlElement(required = true, nillable = true)
    protected String longcode;

    /**
     * Gets the value of the caid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the caid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getCaid() {
        if (caid == null) {
            caid = new ArrayList<>();
        }
        return this.caid;
    }

    /**
     * Gets the value of the castate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the castate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCastate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getCastate() {
        if (castate == null) {
            castate = new ArrayList<>();
        }
        return this.castate;
    }

    /**
     * Gets the value of the caname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the caname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaname().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCaname() {
        if (caname == null) {
            caname = new ArrayList<>();
        }
        return this.caname;
    }

    /**
     * Gets the value of the cault property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the cault property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCault().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getCault() {
        if (cault == null) {
            cault = new ArrayList<>();
        }
        return this.cault;
    }

    /**
     * Gets the value of the caalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the caalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCaalias() {
        if (caalias == null) {
            caalias = new ArrayList<>();
        }
        return this.caalias;
    }

    /**
     * Gets the value of the maid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the maid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMaid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMaid() {
        if (maid == null) {
            maid = new ArrayList<>();
        }
        return this.maid;
    }

    /**
     * Gets the value of the mastate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the mastate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMastate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMastate() {
        if (mastate == null) {
            mastate = new ArrayList<>();
        }
        return this.mastate;
    }

    /**
     * Gets the value of the maname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the maname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManame().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getManame() {
        if (maname == null) {
            maname = new ArrayList<>();
        }
        return this.maname;
    }

    /**
     * Gets the value of the maalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the maalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMaalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getMaalias() {
        if (maalias == null) {
            maalias = new ArrayList<>();
        }
        return this.maalias;
    }

    /**
     * Gets the value of the mapushenabled property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the mapushenabled property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapushenabled().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMapushenabled() {
        if (mapushenabled == null) {
            mapushenabled = new ArrayList<>();
        }
        return this.mapushenabled;
    }

    /**
     * Gets the value of the macid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMacid() {
        if (macid == null) {
            macid = new ArrayList<>();
        }
        return this.macid;
    }

    /**
     * Gets the value of the macstate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macstate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacstate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMacstate() {
        if (macstate == null) {
            macstate = new ArrayList<>();
        }
        return this.macstate;
    }

    /**
     * Gets the value of the macname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacname().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getMacname() {
        if (macname == null) {
            macname = new ArrayList<>();
        }
        return this.macname;
    }

    /**
     * Gets the value of the macalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getMacalias() {
        if (macalias == null) {
            macalias = new ArrayList<>();
        }
        return this.macalias;
    }

    /**
     * Gets the value of the macpushenabled property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macpushenabled property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacpushenabled().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMacpushenabled() {
        if (macpushenabled == null) {
            macpushenabled = new ArrayList<>();
        }
        return this.macpushenabled;
    }

    /**
     * Gets the value of the vaid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vaid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getVaid() {
        if (vaid == null) {
            vaid = new ArrayList<>();
        }
        return this.vaid;
    }

    /**
     * Gets the value of the vastate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vastate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVastate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getVastate() {
        if (vastate == null) {
            vastate = new ArrayList<>();
        }
        return this.vastate;
    }

    /**
     * Gets the value of the vaname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vaname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaname().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getVaname() {
        if (vaname == null) {
            vaname = new ArrayList<>();
        }
        return this.vaname;
    }

    /**
     * Gets the value of the vaalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vaalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getVaalias() {
        if (vaalias == null) {
            vaalias = new ArrayList<>();
        }
        return this.vaalias;
    }

}
