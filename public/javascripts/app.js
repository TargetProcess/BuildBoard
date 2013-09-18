requirejs.config({
    "baseUrl": window.requireBaseUrl,
    "paths": {
        "underscore": "//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.5.2/underscore-min",
        "jquery": "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min",
        "bootstrap": "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min"
    },
    shim:{
        'underscore': {
            exports: '_'
        }
    }
});

requirejs(["app/main"]);