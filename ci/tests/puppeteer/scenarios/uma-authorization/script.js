const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "scope=uma_protection&";
    params += "username=casuser&";
    params += "password=Mellon&";
    params += "grant_type=password";

    let protectionToken = null;
    let url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        'Content-Type': "application/json"
    }, res => {
        protectionToken = res.data.access_token;
    }, error => {
        throw `Operation failed: ${error}`;
    });


    let resourceUrl = `https://localhost:8443/cas/oauth2.0/resourceSet`;
    let resourceObject = {
        uri: "http://api.example.org/photos/**",
        type: "website",
        name: "Photos API",
        resource_scopes: ["create", "read"]
    };
    let resourceRequest = JSON.stringify(resourceObject);
    await cas.log(`Creating resource ${resourceRequest}`);
    let resource = JSON.parse(await cas.doRequest(resourceUrl, "POST",
        {
            "Authorization": `Bearer ${protectionToken}`,
            'Content-Length': resourceRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200, resourceRequest));
    await cas.log(resource);

    const policyUrl = `https://localhost:8443/cas/oauth2.0/${resource.resourceId}/policy`;
    let policyObject = {
        id: 1234,
        permissions: [
            {
                id: 1,
                subject: "casuser",
                scopes: ["read"],
                claims: {
                    first_name: "CAS",
                    last_name: "User"
                }
            },
            {
                id: 2,
                subject: "casuser",
                scopes: ["create"],
                claims: {
                    first_name: "CAS",
                    last_name: "User"
                }
            }
        ]
    };
    let policyRequest = JSON.stringify(policyObject);
    await cas.log(`Creating policy ${policyRequest}`);
    let result = JSON.parse(await cas.doRequest(policyUrl, "POST",
        {
            "Authorization": `Bearer ${protectionToken}`,
            'Content-Length': policyRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200, policyRequest));
    await cas.log(result);

    let permissionObject = {
        resource_id: resource.resourceId,
        resource_scopes: ["read", "create"],
        claims: {
            first_name: "CAS"
        }
    };

    let permissionRequest = JSON.stringify(permissionObject);
    await cas.log(`Creating permission ${permissionRequest}`);
    result = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oauth2.0/permission", "POST",
        {
            "Authorization": `Bearer ${protectionToken}`,
            'Content-Length': permissionRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200, permissionRequest));
    await cas.log(result);
    assert(result.ticket !== null);
    assert(result.code !== null);

    let permissionTicket = result.ticket;
    await cas.log(`Found UMA permission ticket ${permissionTicket}`);

    await cas.log("Checking for UMA JWKS");
    await cas.doGet(`https://localhost:8443/cas/oauth2.0/umaJwks`,
        res => assert(res.status === 200), error => {
            throw error;
        });

    await cas.log("Getting access token for authorization");
    params = "client_id=client&";
    params += "client_secret=secret&";
    params += "scope=uma_authorization read&";
    params += "username=casuser&";
    params += "password=Mellon&";
    params += "grant_type=password";

    let authorizationToken = null;
    url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        'Content-Type': "application/json"
    }, res => {
        authorizationToken = res.data.access_token;
    }, error => {
        throw `Operation failed: ${error}`;
    });

    await cas.log(`Asking for relying party token (RPT) based on ${authorizationToken}`);
    let authzObject = {
        ticket: permissionTicket,
        'grant_type': "urn:ietf:params:oauth:grant-type:uma-ticket"
    };
    let authzRequest = JSON.stringify(authzObject);
    result = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oauth2.0/rptAuthzRequest", "POST",
        {
            "Authorization": `Bearer ${authorizationToken}`,
            'Content-Length': authzRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 308, authzRequest));
    await cas.log(result);
    assert(result.error_details.requesting_party_claims.required_claims !== null);
    assert(result.error_details.requesting_party_claims.required_scopes !== null);


    await cas.log("Executing claim collection...");
    params = `client_id=client&ticket=${permissionTicket}&state=12345&redirect_uri=https://apereo.github.io`;
    await cas.doRequest(`https://localhost:8443/cas/oauth2.0/rqpClaims?${params}`, "GET",
        {
            "Authorization": `Bearer ${protectionToken}`,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 302, undefined,
        res =>
            assert(res.headers.location.includes(
                "https://apereo.github.io?authorization_state=claims_submitted&state=12345")));

    await cas.log(`After claim collection, asking for relying party token (RPT) based on ${authorizationToken}`);
    authzObject = {
        ticket: permissionTicket,
        'grant_type': "urn:ietf:params:oauth:grant-type:uma-ticket"
    };
    authzRequest = JSON.stringify(authzObject);
    result = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oauth2.0/rptAuthzRequest", "POST",
        {
            "Authorization": `Bearer ${authorizationToken}`,
            'Content-Length': authzRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200, authzRequest));
    await cas.log(result);
    assert(result.rpt !== null);

    let value = `client:secret`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    await cas.log(`Authorization header: ${authzHeader}`);
    await cas.doPost(`https://localhost:8443/cas/oauth2.0/introspect?token=${result.rpt}`,
        {},
        {
            'Authorization': authzHeader,
            'Content-Type': 'application/json'
        },
        res => {
            assert(res.data.active === true);
            assert(res.data.grant_type === 'urn:ietf:params:oauth:grant-type:uma-ticket');
            assert(res.data.sub === 'casuser')
        }, error => {
            throw `Introspection operation failed: ${error}`;
        });

})();
