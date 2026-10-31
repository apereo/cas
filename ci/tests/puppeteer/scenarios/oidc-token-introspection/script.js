
const assert = require("assert");
const cas = require("../../cas.js");

const AUTHZ_HEADER = `Basic ${Buffer.from("client:secret").toString("base64")}`;

(async () => {
    const tokens = await issueTokens();
    await introspect(tokens.access_token);
    await introspect(tokens.refresh_token);

    await revoke(tokens.access_token, "access_token");
    await assertInactive(tokens.access_token);
    await introspect(tokens.refresh_token);

    const cascading = await issueTokens();
    await introspect(cascading.access_token);
    await revoke(cascading.refresh_token, "refresh_token");
    await assertInactive(cascading.refresh_token);
    await assertInactive(cascading.access_token);

    await revoke("AT-unknown-token", "access_token");
})();

async function issueTokens() {
    const params = "grant_type=client_credentials&scope=openid";
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    return cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": AUTHZ_HEADER
    }, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        return res.data;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function revoke(token, hint) {
    await cas.log(`Revoking token ${token} with hint ${hint}`);
    const body = new URLSearchParams({token: token, "token_type_hint": hint}).toString();
    await cas.doRequest("https://localhost:8443/cas/oidc/revoke", "POST", {
        "Authorization": AUTHZ_HEADER,
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(body)
    }, 200, body);
}

async function assertInactive(token) {
    await cas.log(`Introspecting revoked token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        (res) => {
            cas.log(res.data);
            assert(res.data.active === false);
            assert(!res.data.sub);
        }, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": AUTHZ_HEADER,
            "Content-Type": "application/json"
        });
}

async function introspect(token) {
    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        (res) => {
            assert(res.data.active === true);
            assert(res.data.aud === "client");
            assert(res.data.sub === "client");
            assert(res.data.iss === "https://localhost:8443/cas/oidc");
            assert(res.data.client_id === "client");
            assert(res.data.token === token);
        }, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": AUTHZ_HEADER,
            "Content-Type": "application/json"
        });

    await cas.log(`Introspecting token ${token} as JWT`);
    const jwt = await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        (res) => res.data, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": AUTHZ_HEADER,
            "Content-Type": "application/json",
            "Accept": "application/token-introspection+jwt"
        });
    const decoded = await cas.decodeJwt(jwt);
    assert(decoded.iss === "https://localhost:8443/cas/oidc");
    assert(decoded.aud === "client");
    assert(decoded.iat !== undefined);
    assert(decoded.jti !== undefined);
    assert(decoded.token_introspection.active === true);
    assert(decoded.token_introspection.aud === "client");
    assert(decoded.token_introspection.sub === "client");
    assert(decoded.token_introspection.token === token);
}
