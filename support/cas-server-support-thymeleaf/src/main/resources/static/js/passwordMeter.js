document.addEventListener("DOMContentLoaded", (event) => {
    let strength = passwordStrengthI18n;

    $.fn.zxcvbnProgressBar = function (options) {

        //init settings
        let settings = $.extend({
            allProgressBarClasses: 'progress-bar-danger progress-bar-warning progress-bar-success progress-bar-striped active',
            progressBarClass0: 'progress-bar-danger',
            progressBarClass1: 'progress-bar-danger',
            progressBarClass2: 'progress-bar-warning',
            progressBarClass3: 'progress-bar-success',
            progressBarClass4: 'progress-bar-success'
        }, options);

        $(settings.passwordInput).keyup(event => UpdateProgressBar());

        return this.each(function () {
            settings.progressBar = this;
            UpdateProgressBar();
        });

        function setProgress(value, bar) {
            let materialBar = bar.foundation;
            if (materialBar) {
                materialBar.setProgress(value > 0 ? value / 100 : 0);
            } else {
                $(bar).find('#progress-strength-indicator').css('width', value + '%');
            }
        }

        function UpdateProgressBar() {
            let progressBar = settings.progressBar;

            let indicator = document.getElementById('progress-strength-indicator');
            let password = document.getElementById('password').value;

            if (password) {
                let result = zxcvbn(password, settings.userInputs);
                //result.score: 0, 1, 2, 3 or 4 - if crack time is less than 10**2, 10**4, 10**6, 10**8, Infinity.
                let scorePercentage = (result.score + 1) * 20;
                setProgress(scorePercentage, settings.bar);

                if (result.score === 0) {
                    //weak
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass0);
                    $(indicator).html(strength[0]);
                }
                else if (result.score === 1) {
                    //normal
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass1);
                    $(indicator).html(strength[1]);
                }
                else if (result.score === 2) {
                    //medium
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass2);
                    $(indicator).html(strength[2]);
                }
                else if (result.score === 3) {
                    //strong
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass3);
                    $(indicator).html(strength[3]);
                }
                else if (result.score === 4) {
                    //very strong
                    $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass4);
                    $(indicator).html(strength[4]);
                }
            } else {
                setProgress(0);
                $(progressBar).removeClass(settings.allProgressBarClasses).addClass(settings.progressBarClass0);
                $(indicator).html('');
            }
        }
    };
    let policyPatternRegex = new RegExp(policyPattern);
    let password = document.getElementById('password');
    let confirmed = document.getElementById('confirmedPassword');
    let current = document.getElementById('currentPassword');
    let barElement = document.getElementById('strengthProgressBar');
    let bar;

    if (barElement !== null && barElement !== undefined) {
        bar = $(barElement);
    }

    if (password !== null && password !== undefined) {
        password.addEventListener('keyup', event => validate());
        password.addEventListener('input', validate);
        confirmed.addEventListener('input', validate);

        if(current !== undefined && current !== null){
            current.addEventListener('keyup', event => validate());
            current.addEventListener('input', validate);
        }
    }

    let alertSettings = {
        allAlertClasses: 'mdi-close-circle mdi-alert-circle mdi-information mdi-check-circle text-danger text-warning text-secondary text-success',
        alertClassDanger: 'mdi-close-circle text-danger',
        alertClassWarning: 'mdi-alert-circle text-warning',
        alertClassInfo: 'mdi-information text-secondary',
        alertClassSuccess: 'mdi-check-circle text-success'
    };

    function validate() {
        let val = password.value;
        let cnf = confirmed.value;
        let crt = current !== null ? current.value : null;

        $('#password-strength-msg').hide();
        $('#password-policy-violation-msg').hide();
        $('#password-confirm-mismatch-msg').hide();

        let passwordPolicyViolated = val === '' || !policyPatternRegex.test(val);
        let passwordMismatch = val !== '' && val !== cnf;
        let result = zxcvbn(val);
        let passwordTooWeak = passwordMinimumStrength > result.score;
        let currentPasswordEmpty = crt !== null && crt === '';
        let disableSubmit = passwordPolicyViolated || passwordMismatch || passwordTooWeak || currentPasswordEmpty;
        $('#submit').prop('disabled', disableSubmit);

        $('#strengthProgressBar').zxcvbnProgressBar({ passwordInput: 'password', bar: bar });

        // Check strength, update the text indicator
        if (val !== '') {
            $('#strengthProgressBar').removeClass('d-none');
            $('#password-strength-warning').text(result.feedback.warning);
            $('#password-strength-suggestions').text(result.feedback.suggestions.join(' ').trim());

            let clz = alertSettings.alertClassDanger;
            switch (result.score) {
                case 0:
                case 1:
                    clz = alertSettings.alertClassDanger;
                    break;
                case 2:
                    clz = alertSettings.alertClassWarning;
                    break;
                case 3:
                    clz = alertSettings.alertClassInfo;
                    break;
                case 4:
                case 5:
                default:
                    clz = alertSettings.alertClassSuccess;
                    break;
            }

            $('#password-strength-icon').removeClass(alertSettings.allAlertClasses).addClass(clz);
            // Check for suggestions
            if (result.feedback.warning > 0 || result.feedback.suggestions.length > 0) {
                $('#password-strength-msg').show();
            }
        } else {
            $('#password-strength-icon').removeClass(alertSettings.allAlertClasses);
            $('#password-strength-warning').text('');
            $('#password-strength-suggestions').text('');
        }

        // Check for mismatch
        if (passwordMismatch && cnf !== '') {
            $('#password-confirm-mismatch-msg').show();
        }

        // Check password policy
        if (passwordPolicyViolated) {
            $('#password-policy-violation-msg').show();
        }
    }
});
