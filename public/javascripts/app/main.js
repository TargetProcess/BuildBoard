require([
    'jquery',
    'underscore',
    'bootstrap',

    'libs/bootstrap-sortable',

    'app/jquery/jquery.navigation',

    'app/navigation',
    'app/branches',
    'app/githubLogin'
], function ($,
             _,
             bootstrap,

             bootstrap_sortable,

             jquery_navigation,

             navigation) {
    $(function () {
        navigation.start({
            user: 'all',
            branches: 'all'
        });
    });
});
