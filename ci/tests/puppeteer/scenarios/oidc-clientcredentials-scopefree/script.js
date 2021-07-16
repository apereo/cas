const https = require('https');
const assert = require('assert');
const axios = require('axios');
const cas = require('../../cas.js');
const jwt = require('jsonwebtoken');

(async () => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    let url = 'https://localhost:8443/cas/oidc/token?' + params;
    console.log("Calling " + url);
    
    instance
        .post(url, new URLSearchParams(), {
            headers: {
                'Content-Type': "application/json"
            }
        })
        .then(res => {
            console.log(res.data);
            assert(res.data.access_token !== null);

            console.log("Decoding JWT access token...");
            let decoded = jwt.decode(res.data.access_token);
            console.log(decoded);
            
            assert(res.data.id_token !== null);
            assert(res.data.refresh_token !== null);
            assert(res.data.token_type !== null);
            assert(res.data.scope !== null);

            console.log("Decoding id token...")
            decoded = jwt.decode(res.data.id_token);
            console.log(decoded);

            assert(decoded.sub !== null)
            assert(decoded.cn !== null)
            assert(decoded.name !== null)
            assert(decoded["preferred_username"] !== null)
            assert(decoded["given-name"] !== null)
        })
        .catch(error => {
            throw 'Operation failed: ' + error;
        })
})();
