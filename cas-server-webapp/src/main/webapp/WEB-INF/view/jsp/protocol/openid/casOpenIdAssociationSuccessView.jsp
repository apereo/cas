<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:out value="ns:${parameters['ns']}" />
<c:out value="enc_mac_key:${parameters['enc_mac_key']}" />
<c:out value="assoc_type:${parameters['assoc_type']}" />
<c:out value="dh_server_public:${parameters['dh_server_public']}" />
<c:out value="session_type:${parameters['session_type']}" />
<c:out value="expires_in:${parameters['expires_in']}" />
<c:out value="assoc_handle:${parameters['assoc_handle']}" />
