// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

(function(root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['base64js'], factory);
    } else if (typeof module === 'object' && module.exports) {
        module.exports = factory(require('base64js'));
    } else {
        root.base64url = factory(root.base64js);
    }
})(this, function(base64js) {

    function ensureUint8Array(arg) {
        if (arg instanceof ArrayBuffer) {
            return new Uint8Array(arg);
        } else {
            return arg;
        }
    }

    function base64UrlToMime(code) {
        return code.replace(/-/g, '+').replace(/_/g, '/') + '===='.substring(0, (4 - (code.length % 4)) % 4);
    }

    function mimeBase64ToUrl(code) {
        return code.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }

    function fromByteArray(bytes) {
        return mimeBase64ToUrl(base64js.fromByteArray(ensureUint8Array(bytes)));
    }

    function toByteArray(code) {
        return base64js.toByteArray(base64UrlToMime(code));
    }

    return {
        fromByteArray: fromByteArray,
        toByteArray: toByteArray,
    };

});
