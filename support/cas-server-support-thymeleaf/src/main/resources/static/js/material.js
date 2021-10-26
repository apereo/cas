(function (material, $) {
    let cas = {
        init: function () {
            cas.attachFields();
            material.autoInit();
        },
        attachFields: function () {
            new material.textField.MDCTextFieldHelperText(document.querySelectorAll('.mdc-text-field-helper-text'));

            let divs = document.querySelectorAll('.mdc-text-field'),
                field;
            let div;
            for (i = 0; i < divs.length; ++i) {
                div = divs[i];
                field = material.textField.MDCTextField.attachTo(div);
                if (div.classList.contains('caps-check')) {
                    field.foundation.adapter.registerInputInteractionHandler('keypress', cas.checkCaps);
                }
            }
            let selector = document.querySelector('.mdc-select');
            if (selector != null) {
                const select = new material.select.MDCSelect(selector);
                select.listen('MDCSelect:change', function () {
                    $('#source').val(select.value);
                });
                $('#source').val(select.value);
            }
        },
        checkCaps: function (ev) {
            let s = String.fromCharCode(ev.which);
            if (s.toUpperCase() === s && s.toLowerCase() !== s && !ev.shiftKey) {
                ev.target.parentElement.classList.add('caps-on');
            } else {
                console.log('caps off')
                ev.target.parentElement.classList.remove('caps-on');
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

        $('.reveal-password').click(function (ev) {
            if ($('.pwd').attr('type') != 'text') {
                $('.pwd').attr('type', 'text');
                $(".reveal-password-icon").removeClass("mdi mdi-eye").addClass("mdi mdi-eye-off");
            } else {
                $('.pwd').attr('type', 'password');
                $(".reveal-password-icon").removeClass("mdi mdi-eye-off").addClass("mdi mdi-eye");
            }
            ev.preventDefault();
        });

        if (typeof (jqueryReady) == 'function') {
            jqueryReady();
        }
    });

}