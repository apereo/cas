const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "scope=uma_protection&";
    params += "username=casuser&";
    params += "password=Mellon&";
    params += "grant_type=password";

    let at = null;
    let url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        'Content-Type': "application/json"
    }, res => {
        at = res.data.access_token;
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
            "Authorization": `Bearer ${at}`,
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
            "Authorization": `Bearer ${at}`,
            'Content-Length': policyRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200, policyRequest));
    await cas.log(result);

    let permissionObject = {
        resource_id: resource.resourceId,
        resource_scopes: ["read"],
        claims: {
            first_name: "CAS"
        }
    };

    let permissionRequest = JSON.stringify(permissionObject);
    await cas.log(`Creating permission ${permissionRequest}`);
    result = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oauth2.0/permission", "POST",
        {
            "Authorization": `Bearer ${at}`,
            'Content-Length': permissionRequest.length,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200, permissionRequest));
    await cas.log(result);
    assert(result.ticket !== null);
    assert(result.code !== null);


    await cas.log("Checking for claims");
    params = `client_id=client&ticket=${result.ticket}&state=12345&redirect_uri=https://apereo.github.io`;
    await cas.doRequest(`https://localhost:8443/cas/oauth2.0/rqpClaims?${params}`, "GET",
        {
            "Authorization": `Bearer ${at}`,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 302, undefined,
        res => {
            cas.log(res.headers);
            assert(res.headers.location.includes(
                "https://apereo.github.io?authorization_state=claims_submitted&state=12345"));
        });
})();
