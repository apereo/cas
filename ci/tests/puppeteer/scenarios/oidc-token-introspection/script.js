const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    console.log(`Calling ${url}`);

    let accessToken = null;
    let refreshToken = null;
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        console.log(res.data);
        assert(res.data.access_token !== null);
        assert(res.data.refresh_token !== null);

        introspect(res.data.access_token);
        introspect(res.data.refresh_token);

    }, error => {
        throw `Operation failed: ${error}`;
    });

})();

async function introspect(token) {
    let value = `client:secret`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    console.log(`Authorization header: ${authzHeader}`);

    console.log(`Introspecting token ${token}`)
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        res => {
            assert(res.data.active === true)
            assert(res.data.aud === "client")
            assert(res.data.sub === "client")
            assert(res.data.iss === "https://localhost:8443/cas/oidc")
            assert(res.data.client_id === "client")
            assert(res.data.token === token)
        }, error => {
            throw `Introspection operation failed: ${error}`;
        }, {
            'Authorization': authzHeader,
            'Content-Type': 'application/json'
        });
}
