/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    var buildBoard = angular.module('buildBoard', ['ui.bootstrap', 'ngRoute'])
        .controller('branchesController', BranchesController)
        .controller('pullRequestController', PullRequestController)
        .filter('activeFilter', activeFilter)
        .config(['$routeProvider',
            ($routeProvider:ng.route.IRouteProvider)=>
                $routeProvider
                    .when('/branches', {
                        templateUrl: '/assets/partials/branchList.html',
                        controller: BranchesController
                    })
                    .otherwise({
                        redirectTo: '/branches'
                    })
        ])


}