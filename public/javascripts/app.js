requirejs.config({
    "baseUrl": "/assets/javascripts",
    "paths": {
        "underscore": "//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.5.2/underscore-min",
        "jquery": "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min",
        "jqueryUI": "//code.jquery.com/ui/1.10.3/jquery-ui",
    }
});

requirejs(["app/main"]);