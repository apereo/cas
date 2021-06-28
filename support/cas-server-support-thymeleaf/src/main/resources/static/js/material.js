(function (material, $) {
    var cas = {
        init: function () {
            cas.attachFields();
            material.autoInit();
        },
        attachFields: function () {
            var divs = document.querySelectorAll('.mdc-text-field'),
                field;
            var div;
            for (i = 0; i < divs.length; ++i) {
                div = divs[i];
                field = material.textField.MDCTextField.attachTo(div);
                if (div.classList.contains('caps-check')) {
                    field.foundation.adapter.registerInputInteractionHandler('keypress', cas.checkCaps);
                }
            }
            let selector = document.querySelector('.mdc-select.authn-source');
            if (selector != null) {
                const select = new material.select.MDCSelect(selector);
                select.listen('MDCSelect:change', function () {
                    $('#source').val(select.value);
                });
                $('#source').val(select.value);
            }
        },
        checkCaps: function (ev) {
            var s = String.fromCharCode(ev.which),
                parent = $(ev.target).parents('.mdc-input-group');
            if (s.toUpperCase() === s && s.toLowerCase() !== s && !ev.shiftKey) {
                parent.addClass('caps-on');
            } else {
                parent.removeClass('caps-on');
            }
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        cas.init();
    });
})(mdc, jQuery);

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
        $('#fm1 input[name="username"],[name="password"]').trigger('input');
        $('#fm1 input[name="username"]').focus();

        var $revealpassword = $('.reveal-password');
        $revealpassword.mouseup(function (ev) {
            $('.pwd').attr('type', 'password');
            $(".reveal-password-icon").removeClass("mdi mdi-eye-off").addClass("mdi mdi-eye");
            ev.preventDefault();
        })

        $revealpassword.mousedown(function (ev) {
            $('.pwd').attr('type', 'text');
            $(".reveal-password-icon").removeClass("mdi mdi-eye").addClass("mdi mdi-eye-off");
            ev.preventDefault();
        });

        if (typeof (jqueryReady) == 'function') {
            jqueryReady();
        }
    });

}