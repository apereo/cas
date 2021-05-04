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

    instance
        .post('https://localhost:8443/cas/actuator/awsSts?duration=PT15S', params, {
            headers: {
                'Content-Type': "application/x-www-form-urlencoded"
            }
        })
        .then(res => {
            console.log(res.data);
            assert(res.status === 200)
            
            let data = res.data.toString();
            assert(data.includes("aws_access_key_id"));
            assert(data.includes("aws_secret_access_key"));
            assert(data.includes("aws_session_token"));
        })
        .catch(error => {
            console.log(error);
            throw 'Unable to fetch credentials';
        })
})();
