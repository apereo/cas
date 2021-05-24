const assert = require('assert');
const https = require('https');
const axios = require('axios');
const cas = require('../../cas.js');

(async () => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    let params = new URLSearchParams()
    params.append('username', 'user1+casuser')
    params.append('password', 'Mellon')

    instance
        .post('https://localhost:8443/cas/v1/users', params, {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .then(res => {
            console.log(res.data.authentication.attributes);
            assert(res.data.authentication.attributes.surrogateUser != null);
            assert(res.data.authentication.attributes.surrogateEnabled != null);
            assert(res.data.authentication.attributes.surrogatePrincipal != null);
        })
        .catch(error => {
            throw error;
        })

    params = new URLSearchParams()
    params.append('username', 'casuser')
    params.append('password', 'Mellon')

    instance
        .post('https://localhost:8443/cas/v1/users', params, {
            headers: {
                'Accept': 'application/json',
                'X-Surrogate-Principal': 'user1',
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .then(res => {
            console.log(res.data.authentication.attributes);
            assert(res.data.authentication.attributes.surrogateUser != null);
            assert(res.data.authentication.attributes.surrogateEnabled != null);
            assert(res.data.authentication.attributes.surrogatePrincipal != null);
        })
        .catch(error => {
            throw error;
        })
})();
