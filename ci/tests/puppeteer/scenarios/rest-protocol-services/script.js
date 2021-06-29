const https = require('https');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    let service = {
      "@class" : "org.apereo.cas.services.RegexRegisteredService",
      "serviceId" : "https://apereo.github.io/cas",
      "name" : "CAS",
      "id" : 1234,
      "description": "This is the Apereo CAS server"
    };
    let body = JSON.stringify(service);
    console.log("Sending\n" + body);

    let options = {
        protocol: 'https:',
        hostname: 'localhost',
        port: 8443,
        path: '/cas/v1/services',
        method: 'POST',
        rejectUnauthorized: false,
        headers: {
            'Authorization' : 'Basic Y2FzdXNlcjpNZWxsb24=',
            'Accept': 'application/json',
            'Content-Length': body.length,
            'Content-Type': 'application/json'
        }
    };


    const httpPost = options => {
        return new Promise((resolve, reject) => {
            let req = https
                .request(options, res => {
                    assert(res.statusCode === 200);
                    
                    res.setEncoding('utf8');
                    const body = [];
                    res.on('data', chunk => body.push(chunk));
                    res.on('end', () => resolve(body.join('')));
                })
                .on('error', reject);
            req.write(body);
        });
    };
    body = await httpPost(options);
    console.log(body);
   
})();
