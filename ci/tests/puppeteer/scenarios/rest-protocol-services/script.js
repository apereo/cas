const cas = require("../../cas.js");

(async () => {
    const service = {
        "@class": "org.apereo.cas.services.CasRegisteredService",
        "serviceId": "https://localhost:9859/anything/cas",
        "name": "CAS",
        "id": 1234,
        "description": "This is the Apereo CAS server"
    };
    let body = JSON.stringify(service, undefined, 2);
    await cas.log(`Sending ${body}`);

    body = await cas.doRequest("https://localhost:8443/cas/v1/services", "POST", {
        "Authorization": "Basic Y2FzdXNlcjpNZWxsb24=",
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 200, body);
    await cas.log(body);
})();
