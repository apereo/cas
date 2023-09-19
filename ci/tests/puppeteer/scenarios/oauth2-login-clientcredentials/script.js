const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "scope=example&";
    params += "grant_type=client_credentials&";
    let url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        'Content-Type': "application/json"
    }, res => {
        cas.log(res.data);
        assert(res.data.access_token !== null);
        cas.decodeJwt(res.data.access_token, true).then(decoded => {
            assert(decoded !== null);
            assert(decoded.payload["sub"] === "client");
            assert(decoded.payload.client_id === "client");
            assert(decoded.payload.grant_type === "client_credentials")
        });

    }, error => {
        throw `Operation failed: ${error}`;
    });
})();
