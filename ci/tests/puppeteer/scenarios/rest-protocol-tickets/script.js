const https = require('https');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    let opts = await getRequestOptions('/cas/v1/tickets?username=casuser&password=Mellon', 'POST');
    const tgt = await executeRequest(opts, 201);
    console.log(tgt);
    assert(tgt != null);

    opts = await getRequestOptions('/cas/v1/tickets/' + tgt, 'GET');
    await executeRequest(opts, 200);
    
    opts = await getRequestOptions('/cas/v1/tickets/' + tgt + '?service=https://github.com/apereo/cas', 'POST');
    let st = await executeRequest(opts, 200);
    console.log(st);
    assert(st != null);

    opts = await getRequestOptions('/cas/v1/tickets/' + st, 'GET');
    await executeRequest(opts, 200);

    opts = await getRequestOptions('/cas/v1/tickets/' + st, 'DELETE');
    await executeRequest(opts, 200);

    opts = await getRequestOptions('/cas/v1/tickets/' + tgt, 'DELETE');
    await executeRequest(opts, 200);
    
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
