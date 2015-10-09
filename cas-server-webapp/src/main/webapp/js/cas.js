/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


head.ready(document, function() {
    head.load("https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js", loadjQueryUI);
});

function loadjQueryUI() {
    head.load("https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js", loadjQueryCookies);
}

function loadjQueryCookies() {
    head.load("https://cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.4.1/jquery.cookie.min.js", loadjQueryDebug);
}

function loadjQueryDebug() {
    head.load("https://rawgithub.com/cowboy/javascript-debug/master/ba-debug.min.js", resourceLoadedSuccessfully);
}

function areCookiesEnabled() {
    $.cookie('cookiesEnabled', 'true');
    var value = $.cookie('cookiesEnabled');
    if (value != undefined) {
        $.removeCookie('cookiesEnabled');
        return true;
    }
    return false;
}

function resourceLoadedSuccessfully() {
    $(document).ready(function() {

        if ($(":focus").length === 0){
            $("input:visible:enabled:first").focus();
        }


        if (areCookiesEnabled()) {
            $('#cookiesDisabled').hide();
        } else {
            $('#cookiesDisabled').show();
            $('#cookiesDisabled').animate({ backgroundColor: 'rgb(187,0,0)' }, 30).animate({ backgroundColor: 'rgb(255,238,221)' }, 500);
        }

        //flash error box
        $('#msg.errors').animate({ backgroundColor: 'rgb(187,0,0)' }, 30).animate({ backgroundColor: 'rgb(255,238,221)' }, 500);

        //flash success box
        $('#msg.success').animate({ backgroundColor: 'rgb(51,204,0)' }, 30).animate({ backgroundColor: 'rgb(221,255,170)' }, 500);

        //flash confirm box
        $('#msg.question').animate({ backgroundColor: 'rgb(51,204,0)' }, 30).animate({ backgroundColor: 'rgb(221,255,170)' }, 500);

        $('#capslock-on').hide();
        $('#password').keypress(function(e) {
            var s = String.fromCharCode( e.which );
            if ( s.toUpperCase() === s && s.toLowerCase() !== s && !e.shiftKey ) {
                $('#capslock-on').show();
            } else {
                $('#capslock-on').hide();
            }
        });
        if (typeof(jqueryReady) == "function") {
            jqueryReady();
        }
    });

};
