package org.apereo.cas.configuration.model.support.saml.sps;

import com.google.common.collect.Lists;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlServiceProviderProperties {
    private Dropbox dropbox = new Dropbox();
    private Workday workday = new Workday();
    private SAManage saManage = new SAManage();
    private Salesforce salesforce = new Salesforce();
    private Box box = new Box();

    public Box getBox() {
        return box;
    }

    public void setBox(final Box box) {
        this.box = box;
    }

    public Salesforce getSalesforce() {
        return salesforce;
    }

    public void setSalesforce(final Salesforce salesforce) {
        this.salesforce = salesforce;
    }

    public SAManage getSaManage() {
        return saManage;
    }

    public void setSaManage(final SAManage saManage) {
        this.saManage = saManage;
    }

    public Workday getWorkday() {
        return workday;
    }

    public void setWorkday(final Workday workday) {
        this.workday = workday;
    }

    public Dropbox getDropbox() {
        return dropbox;
    }

    public void setDropbox(final Dropbox dropbox) {
        this.dropbox = dropbox;
    }

    public static class Dropbox extends AbstractSamlSPProperties {
        public Dropbox() {
            setNameIdAttribute("mail");
        }
    }

    public static class Box extends AbstractSamlSPProperties {
        public Box() {
            setAttributes(Lists.newArrayList("email", "firstName", "lastName"));
        }
    }
    
    public static class SAManage extends AbstractSamlSPProperties {
        public SAManage() {
            setNameIdAttribute("mail");
        }
    }

    public static class Workday extends AbstractSamlSPProperties {
    }

    public static class Salesforce extends AbstractSamlSPProperties {
        public Salesforce() {
            setAttributes(Lists.newArrayList("mail", "eduPersonPrincipalName"));
        }
    }
}
