const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    let accessToken = null;
    let refreshToken = null;
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        cas.log(res.data);
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
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        res => {
            assert(res.data.active === true);
            assert(res.data.aud === "client");
            assert(res.data.sub === "client");
            assert(res.data.iss === "https://localhost:8443/cas/oidc");
            assert(res.data.client_id === "client");
            assert(res.data.token === token)
        }, error => {
            throw `Introspection operation failed: ${error}`;
        }, {
            'Authorization': authzHeader,
            'Content-Type': 'application/json'
        });

    await cas.log(`Introspecting token ${token} as JWT`);
    let jwt = await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        res => res.data, error => {
            throw `Introspection operation failed: ${error}`;
        }, {
            'Authorization': authzHeader,
            'Content-Type': 'application/json',
            'Accept': 'application/token-introspection+jwt'
        });
    let decoded = await cas.decodeJwt(jwt);
    assert(decoded.iss === "https://localhost:8443/cas/oidc");
    assert(decoded.aud === "client");
    assert(decoded.iat !== undefined);
    assert(decoded.jti !== undefined);
    assert(decoded.token_introspection.active === true);
    assert(decoded.token_introspection.aud === "client");
    assert(decoded.token_introspection.sub === "client");
    assert(decoded.token_introspection.token === token);
}
