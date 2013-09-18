require([
    'jquery',
    'underscore',
    'bootstrap',

    'libs/bootstrap-sortable',


    'app/navigation',
    'app/branches',
    'app/githubLogin'
], function ($,
             _,
             bootstrap,

             bootstrap_sortable,


             navigation) {
        navigation.start({
            user: 'all',
            branches: 'all'
        });
});
