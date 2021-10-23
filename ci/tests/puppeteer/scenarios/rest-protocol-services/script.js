const cas = require('../../cas.js');

(async () => {
    let service = {
        "@class": "org.apereo.cas.services.RegexRegisteredService",
        "serviceId": "https://apereo.github.io/cas",
        "name": "CAS",
        "id": 1234,
        "description": "This is the Apereo CAS server"
    };
    let body = JSON.stringify(service);
    console.log(`Sending ${body}`);

    body = await cas.doRequest("https://localhost:8443/cas/v1/services", "POST", {
        'Authorization': 'Basic Y2FzdXNlcjpNZWxsb24=',
        'Accept': 'application/json',
        'Content-Length': body.length,
        'Content-Type': 'application/json'
    }, 200, body);
    console.log(body);
})();
