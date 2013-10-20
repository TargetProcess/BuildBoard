'use strict';

/* Filters */

angular.module('phonecatFilters', []).filter('activeFilter', function() {
    return function(isActive) {
        return isActive ? 'active' : '';
    };
});