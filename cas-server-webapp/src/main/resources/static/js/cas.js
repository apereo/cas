
head.ready(document, function() {
    if (!window.jQuery) {
        head.load("https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js", loadjQueryUI);
    } else {
        resourceLoadedSuccessfully();
    }
});

function loadjQueryUI() {
    head.load("https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js", loadjQueryCookies);
}

function loadjQueryCookies() {
    head.load("https://cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.4.1/jquery.cookie.min.js", resourceLoadedSuccessfully);
}

function requestGeoPosition() {
    console.log("Requesting GeoLocation data from the browser...");
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(showGeoPosition, logGeoLocationError,
            {maximumAge:600000, timeout:5000, enableHighAccuracy: true});
    } else {
        console.log("Browser does not support Geo Location");
    }
}

function logGeoLocationError(error) {
    switch(error.code) {
        case error.PERMISSION_DENIED:
            console.log("User denied the request for Geolocation.");
            break;
        case error.POSITION_UNAVAILABLE:
            console.log("Location information is unavailable.");
            break;
        case error.TIMEOUT:
            console.log("The request to get user location timed out.");
            break;
        default:
            console.log("An unknown error occurred.");
            break;
    }
}

function showGeoPosition(position) {
    $('[name="geolocation"]').val(position.coords.latitude + ","
        + position.coords.longitude + "," + position.coords.accuracy + "," + position.timestamp);
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

        if (trackGeoLocation) {
            requestGeoPosition();
        }

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
