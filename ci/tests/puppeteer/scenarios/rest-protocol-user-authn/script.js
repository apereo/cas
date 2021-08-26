const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const body = await cas.doRequest('https://localhost:8443/cas/v1/users?username=casuser&password=Mellon', 'POST', {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded'
    });
    console.log(body);
    let result = JSON.parse(body);
    assert(result.authentication.principal.id === "casuser");
})();
