const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    console.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, async function (res) {

        console.log(res.data);
        assert(res.data.access_token !== null);

        console.log("Decoding JWT access token...");
        let decoded = await cas.decodeJwt(res.data.id_token);

        assert(res.data.id_token !== null);
        assert(res.data.refresh_token !== null);
        assert(res.data.token_type !== null);
        assert(res.data.scope !== null);
        assert(decoded.sub !== null)
        assert(decoded.cn !== null)
        assert(decoded.name !== null)
        assert(decoded["preferred_username"] !== null)
        assert(decoded["given-name"] !== null)
    }, function (error) {
        throw `Operation failed: ${error}`;
    });
})();
