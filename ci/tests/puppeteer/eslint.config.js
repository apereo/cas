const js = require("@eslint/js");
const globals = require("globals");

module.exports = [
    js.configs.recommended,
    {
        languageOptions: {
            ecmaVersion: "latest",
            sourceType: "commonjs",
            globals: {
                ...globals.browser,
                ...globals.node,
                ...globals.commonjs,
                ...globals.es2021
            }
        },
        rules: {
            "indent": [
                "error",
                4
            ],
            "linebreak-style": [
                "error",
                "unix"
            ],
            "no-inline-comments": "error",
            "line-comment-position": [
                "error",
                {
                    "position": "above"
                }
            ],
            "no-multiple-empty-lines": [
                "error",
                {
                    "max": 1,
                    "maxEOF": 1
                }
            ],
            "quotes": "error",
            "semi": "error",
            "curly": "error",
            "prefer-template": "error",
            "no-invalid-regexp": "error",
            "no-invalid-this": "error",
            "no-empty-pattern": "error",
            "no-fallthrough": "error",
            "no-dupe-args": "error",
            "no-else-return": "error",
            "no-duplicate-case": "error",
            "no-duplicate-imports": "error",
            "no-dupe-else-if": "error",
            "no-const-assign": "error",
            "no-delete-var": "error",
            "comma-dangle": "error",
            "no-useless-computed-key": "error",
            "consistent-this": "error",
            "arrow-body-style": "error",
            "arrow-parens": "error",
            "semi-style": "error",
            "no-misleading-character-class": "error",
            "prefer-arrow-callback": "error",
            "no-return-await": "error",
            "no-unreachable": "error",
            "no-unreachable-loop": "error",
            "no-useless-concat": "error",
            "no-useless-escape": "error",
            "no-useless-return": "error",
            "eqeqeq": "error",
            "prefer-const": "error",
            "require-atomic-updates": "error",
            "no-alert": "error",
            "no-var": "error",
            "no-redeclare": "error",
            "no-unused-vars": "error",
            "no-useless-assignment": "error",
            "no-useless-call": "error",
            "no-useless-catch": "error",
            "no-multi-assign": "error",
            "yoda": "error"
        }
    }
];
