const https = require('https');
const assert = require('assert');
const jwt = require('jsonwebtoken');
const cas = require('../../cas.js');

(async () => {
    console.log("Creating ticket-granting ticket as JWT")
    let opts = await getRequestOptions('/cas/v1/tickets?username=casuser&password=Mellon&token=true', 'POST');
    let tgt = await executeRequest(opts, 201);
    console.log(tgt);
    let decoded = jwt.decode(tgt);
    console.log(decoded);
    assert(decoded != null);

    console.log("Creating service ticket as JWT")
    opts = await getRequestOptions('/cas/v1/tickets?username=casuser&password=Mellon', 'POST');
    tgt = await executeRequest(opts, 201);
    console.log(tgt);
    assert(tgt != null);

    opts = await getRequestOptions('/cas/v1/tickets/' + tgt + '?service=https://github.com/apereo/cas', 'POST');
    let st = await executeRequest(opts, 200);
    console.log(st);
    decoded = jwt.decode(st);
    console.log(decoded);
    assert(decoded != null);
})();

async function getRequestOptions(path, method) {
    return {
        protocol: 'https:',
        hostname: 'localhost',
        port: 8443,
        path: path,
        method: method,
        rejectUnauthorized: false,
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    };
}

async function executeRequest(requestOptions, statusCode) {
    let httpPost = options => {
        return new Promise((resolve, reject) => {
            https.get(options, res => {
                console.log("Response status code: " + res.statusCode)
                assert(res.statusCode === statusCode);
                res.setEncoding('utf8');
                const body = [];
                res.on('data', chunk => body.push(chunk));
                res.on('end', () => resolve(body.join('')));
            }).on('error', reject);
        });
    };
    return await httpPost(requestOptions);
}
