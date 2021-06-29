const https = require('https');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    let options = {
        protocol: 'https:',
        hostname: 'localhost',
        port: 8443,
        path: '/cas/v1/users?username=casuser&password=Mellon',
        method: 'POST',
        rejectUnauthorized: false,
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    };

    const httpPost = options => {
        return new Promise((resolve, reject) => {
            https.get(options, res => {
                res.setEncoding('utf8');
                const body = [];
                res.on('data', chunk => body.push(chunk));
                res.on('end', () => resolve(body.join('')));
            }).on('error', reject);
        });
    };
    const body = await httpPost(options);
    console.log(body);
    let result = JSON.parse(body);
    assert(result.authentication.principal.id === "casuser");
})();
