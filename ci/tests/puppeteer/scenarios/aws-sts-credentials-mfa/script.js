const https = require('https');
const assert = require('assert');
const axios = require('axios');

(async () => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    const params = new URLSearchParams()
    params.append('username', 'casuser')
    params.append('password', 'Mellon')
    params.append('passcode', '352410')

    instance
        .post('https://localhost:8443/cas/actuator/awsSts?duration=PT15S', params, {
            headers: {
                'Content-Type': "application/x-www-form-urlencoded"
            }
        })
        .then(res => {
            console.log(res.data);
            throw 'Operation must fail to fetch credentials without mfa';
        })
        .catch(error => {
            assert(error.response.status === 401)
            assert(error.response.data.toString().includes("Authentication failed"));
        })
})();
