const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    let url = "https://localhost:8443/cas/oauth2.0/.well-known/uma-configuration";
    await cas.doGet(url, async (res) => {
        assert(res.data.issuer !== undefined);
        assert(res.data.rpt_endpoint !== undefined);
        assert(res.data.permission_registration_endpoint !== undefined);
        assert(res.data.resource_set_registration_endpoint !== undefined);
        assert(res.data.requesting_party_claims_endpoint !== undefined);
    }, async (error) => {
        throw `Operation failed: ${error}`;
    });

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "scope=uma_protection&";
    params += "username=casuser&";
    params += "password=Mellon&";
    params += "grant_type=password";

    let at = null;
    url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
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
    let resourceRequest = JSON.stringify(resourceObject);
    await cas.log(`Creating resource ${resourceRequest}`);
    const resource = JSON.parse(await cas.doRequest(resourceUrl, "POST",
        {
            "Authorization": `Bearer ${at}`,
            "Content-Length": resourceRequest.length,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, resourceRequest));
    await cas.log(resource);
    assert(resource.entity.id !== undefined);
    assert(resource.entity.name !== undefined);
    assert(resource.entity.uri !== undefined);
    assert(resource.entity.type !== undefined);
    assert(resource.entity.owner !== undefined);
    assert(resource.entity.clientId !== undefined);
    assert(resource.code !== undefined);
    assert(resource.location !== undefined);
    assert(resource.resourceId !== undefined);

    await cas.log("Checking for all available resources");
    let result = JSON.parse(await cas.doRequest(resourceUrl, "GET",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, resourceRequest));
    await cas.log(result);

    await cas.log(`Checking for created resource ${resource.location}`);
    result = JSON.parse(await cas.doRequest(resource.location, "GET",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, resourceRequest));
    await cas.log(result);

    resourceObject.name = "Updated Photos API";
    resourceObject.id = resource.resourceId;
    resourceRequest = JSON.stringify(resourceObject);
    await cas.log(`Updating created resource ${resourceRequest}`);
    result = JSON.parse(await cas.doRequest(resource.location, "PUT",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Length": resourceRequest.length,
            "Content-Type": "application/json"
        }, 200, resourceRequest));
    await cas.log(result);
    assert(result.entity.name === "Updated Photos API");

    await cas.log(`Checking for created resource ${resource.location}`);
    result = JSON.parse(await cas.doRequest(resource.location, "GET",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, resourceRequest));
    await cas.log(result);

    await cas.log(`Deleting created resource ${resource.location}`);
    result = JSON.parse(await cas.doRequest(resource.location, "DELETE",
        {
            "Authorization": `Bearer ${at}`,
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200, resourceRequest));
    await cas.log(result);
})();
