const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    let value = `casuser:pa$$word`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    console.log(`Authorization header: ${authzHeader}`);

    const url = "https://localhost:8443/cas/actuator/status";
    let body = await cas.doRequest(url, "GET",
        {
            'Authorization': authzHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36'
        }, 200);
    console.log(body);
    let json = JSON.parse(body);
    assert(json.status !== undefined);
    assert(json.health !== undefined);
    assert(json.host !== undefined);
    assert(json.server !== undefined);
    assert(json.version !== undefined)
})();

