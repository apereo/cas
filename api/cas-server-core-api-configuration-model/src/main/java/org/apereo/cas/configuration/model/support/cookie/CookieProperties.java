package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Common properties for all cookie configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-cookie", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CookieProperties")
public class CookieProperties implements Serializable {

    private static final long serialVersionUID = 6804770601645126835L;

    /**
     * Cookie name. Constructs a cookie with a specified name and value.
     * The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and
     * cannot contain commas, semicolons, or white space or begin with a {@code $} character.
     * The cookie's name cannot be changed after creation.
     * By default, cookies are created according to the RFC 2965 cookie specification.
     * Cookie names are automatically calculated assigned by CAS at runtime, and there is usually
     * no need to customize the name or assign it a different value unless a special use case warrants the change.
     */
    private String name;

    /**
     * Cookie path.
     * Specifies a path for the cookie to which the client should return the cookie.
     * The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's
     * subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog,
     * which makes the cookie visible to all directories on the server under /catalog.
     * Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
     */
    private String path = StringUtils.EMPTY;

    /**
     * Cookie domain. Specifies the domain within which this cookie should be presented.
     * The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com)
     * and means that the cookie is visible to servers in a
     * specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com).
     * By default, cookies are only returned to the server that sent them.
     */
    private String domain = StringUtils.EMPTY;

    /**
     * CAS Cookie comment, describes the cookie's usage and purpose.
     */
    private String comment = "CAS Cookie";

    /**
     * True if sending this cookie should be restricted to a secure protocol, or
     * false if the it can be sent using any protocol.
     */
    private boolean secure = true;

    /**
     * true if this cookie contains the HttpOnly attribute. This means that the cookie should
     * not be accessible to scripting engines, like javascript.
     */
    private boolean httpOnly = true;

    /**
     * The maximum age of the cookie, specified in seconds. By default, {@code -1} indicating
     * the cookie will persist until browser shutdown.
     * A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is
     * the maximum age when the cookie will expire, not the cookie's current age.
     * A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits.
     * A zero value causes the cookie to be deleted.
     */
    private int maxAge = -1;

    /**
     * If a cookie is only intended to be accessed in a first party context, the
     * developer has the option to apply one of settings
     * {@code SameSite=Lax} or {@code SameSite=Strict} or {@code SameSite=None} to prevent external access.
     * <p>
     * To safeguard more websites and their users, the new secure-by-default model
     * assumes all cookies should be protected from external access unless otherwise
     * specified. Developers must use a new cookie setting, {@code SameSite=None}, to designate
     * cookies for cross-site access. When the {@code SameSite=None} attribute is present, an additional
     * {@code Secure} attribute is used so cross-site cookies can only be accessed over HTTPS
     * connections.
     * </p>
     * <p>Accepted values are: {@code Lax}, {@code Strict},  {@code None}.</p>
     */
    private String sameSitePolicy = StringUtils.EMPTY;
}
