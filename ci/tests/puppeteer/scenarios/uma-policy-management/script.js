const cas = require("../../cas.js");

(async () => {
    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "scope=uma_protection&";
    params += "username=casuser&";
    params += "password=Mellon&";
    params += "grant_type=password";

    let at = null;
    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        "Content-Type": "application/json"
    }, (res) => {
        at = res.data.access_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const resourceUrl = "https://localhost:8443/cas/oauth2.0/resourceSet";
    const resourceObject = {
        uri: "http://api.example.org/photos/**",
        type: "website",
        name: "Photos API",
        resource_scopes: ["create", "read"]
    };
    const resourceRequest = JSON.stringify(resourceObject);
    await cas.log(`Creating resource ${resourceRequest}`);
    const resource = JSON.parse(await cas.doRequest(resourceUrl, "POST",
        {
            "Authorization": `Bearer ${at}`,
            "Content-Length": resourceRequest.length,
            "Accept": "application/json",
            "Content-Type": "application/json"
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
            "Content-Length": policyRequest.length,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, policyRequest));
    await cas.log(result);
    await cas.log(JSON.stringify(result.entity.policies));

    await cas.log(`Checking for created policy ${policyRequest}`);
    result = JSON.parse(await cas.doRequest(`${policyUrl}/1234`, "GET",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, policyRequest));
    await cas.log(result);

    await cas.log("Checking for all created policies");
    result = JSON.parse(await cas.doRequest(`${policyUrl}`, "GET",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, policyRequest));
    await cas.log(result);

    policyObject = {
        id: 1234,
        permissions: [
            {
                id: 1,
                subject: "casuser",
                scopes: ["read"],
                claims: {
                    first_name: "Apereo",
                    last_name: "CAS"
                }
            }
        ]
    };
    policyRequest = JSON.stringify(policyObject);
    await cas.log(`Updating policy ${policyRequest}`);
    result = JSON.parse(await cas.doRequest(`${policyUrl}/1234`, "PUT",
        {
            "Authorization": `Bearer ${at}`,
            "Content-Length": policyRequest.length,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, policyRequest));
    await cas.log(result);
    await cas.log(JSON.stringify(result.entity.policies));

    await cas.log("Deleting policy");
    result = JSON.parse(await cas.doRequest(`${policyUrl}/1234`, "DELETE",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200));
    await cas.log(result);

    await cas.log("Checking for all created policies");
    result = JSON.parse(await cas.doRequest(`${policyUrl}`, "GET",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, policyRequest));
    await cas.log(result);
})();
