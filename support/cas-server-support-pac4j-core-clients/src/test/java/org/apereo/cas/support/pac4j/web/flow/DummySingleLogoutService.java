package org.apereo.cas.support.pac4j.web.flow;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.NamespaceManager;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.util.AttributeMap;
import org.opensaml.core.xml.util.IDIndex;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.collection.LockableClassToInstanceMultiMap;


/**
 * Dummy implementation of {@link SingleLogoutService} just for unit tests.
 * 
 * Does not do anything.
 * 
 * @author jkacer
 */
public class DummySingleLogoutService implements SingleLogoutService {

    @Override
    public String getBinding() {
        return null;
    }

    @Override
    public void setBinding(final String binding) {}

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public void setLocation(final String location) {}

    @Override
    public String getResponseLocation() {
        return null;
    }

    @Override
    public void setResponseLocation(final String location) {}

    @Override
    public void detach() {}

    @Override
    public Element getDOM() {
        return null;
    }

    @Override
    public QName getElementQName() {
        return null;
    }

    @Override
    public IDIndex getIDIndex() {
        return null;
    }

    @Override
    public NamespaceManager getNamespaceManager() {
        return null;
    }

    @Override
    public Set<Namespace> getNamespaces() {
        return null;
    }

    @Override
    public String getNoNamespaceSchemaLocation() {
        return null;
    }

    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

    @Override
    public XMLObject getParent() {
        return null;
    }

    @Override
    public String getSchemaLocation() {
        return null;
    }

    @Override
    public QName getSchemaType() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public void releaseChildrenDOM(final boolean propagateRelease) {}

    @Override
    public void releaseDOM() {}

    @Override
    public void releaseParentDOM(final boolean propagateRelease) {}

    @Override
    public XMLObject resolveID(final String id) {
        return null;
    }

    @Override
    public XMLObject resolveIDFromRoot(final String id) {
        return null;
    }

    @Override
    public void setDOM(Element dom) {}

    @Override
    public void setNoNamespaceSchemaLocation(final String location) {}

    @Override
    public void setParent(XMLObject parent) {}

    @Override
    public void setSchemaLocation(final String location) {}

    @Override
    public Boolean isNil() {
        return null;
    }

    @Override
    public XSBooleanValue isNilXSBoolean() {
        return null;
    }

    @Override
    public void setNil(final Boolean newNil) {}

    @Override
    public void setNil(final XSBooleanValue newNil) {}

    @Override
    public LockableClassToInstanceMultiMap<Object> getObjectMetadata() {
        return null;
    }

    @Override
    public List<XMLObject> getUnknownXMLObjects() {
        return null;
    }

    @Override
    public List<XMLObject> getUnknownXMLObjects(final QName typeOrName) {
        return null;
    }

    @Override
    public AttributeMap getUnknownAttributes() {
        return null;
    }

}
