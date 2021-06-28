function resourceLoadedSuccessfully() {

    $(document).ready(function () {

        if (trackGeoLocation) {
            requestGeoPosition();
        }

        if ($(':focus').length === 0) {
            $('input:visible:enabled:first').focus();
        }

        preserveAnchorTagOnForm();
        preventFormResubmission();

        $('.caps-warn').hide();
        $('#fm1 input[name="username"],[name="password"]').trigger('input');
        $('#fm1 input[name="username"]').focus();

        $('#password').keypress(function (e) {
            var s = String.fromCharCode(e.which);
            if (s.toUpperCase() === s && s.toLowerCase() !== s && !e.shiftKey) {
                $('.caps-warn').show();
            } else {
                $('.caps-warn').hide();
            }
        });

        var $revealpassword = $('.reveal-password');
        $revealpassword.mouseup(function (ev) {
            $('.pwd').attr('type', 'password');
            $(".reveal-password-icon").removeClass("fas fa-eye-slash").addClass("fas fa-eye");
            ev.preventDefault();
        })

        $revealpassword.mousedown(function (ev) {
            $('.pwd').attr('type', 'text');
            $(".reveal-password-icon").removeClass("fas fa-eye").addClass("fas fa-eye-slash");
            ev.preventDefault();
        });

        if (typeof(jqueryReady) == 'function') {
            jqueryReady();
        }
    });

}
