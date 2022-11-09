const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const body = await cas.doRequest("https://localhost:8443/cas/v1/users?username=casuser&password=Mellon&passcode=123056", "POST", {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded'
    }, 401);
    console.log(body);
    let result = JSON.parse(body);
    assert(result.authentication_exceptions != null);
})();
