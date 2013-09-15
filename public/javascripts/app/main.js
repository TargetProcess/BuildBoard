require([
    'jquery',
    'underscore',
    'bootstrap',
    'libs/bootstrap-sortable',

    'app/navigation',
    'app/branches',
    'app/githubLogin'
], function ($, _, _1, _2, navigation) {
    $(function () {
        navigation.start({
            user: 'all',
            branches: 'all'
        });
    });
});
