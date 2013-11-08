/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    angular.module('buildBoard', ['ui.bootstrap', 'ngRoute'])
        .service(ServerRoutesService.NAME, ServerRoutesService)
        .controller('branchesController', BranchesController)
        .controller('pullRequestController', PullRequestController)
        .filter('activeFilter', activeFilter)
        .filter('encode', encode)
        .directive('entityTitle', ()=>new EntityTitleDirective())
        .config(['$routeProvider',
            ($routeProvider:ng.route.IRouteProvider)=>
                $routeProvider
                    .when('/branches', {
                        templateUrl: '/assets/partials/branchList.html',
                        controller: BranchesController
                    })
                    .when('/branches/:branchId', {
                        templateUrl: '/assets/partials/branch.html',
                        controller: BranchController
                    })
                    .when('/branches/:branchType/:branchId', {
                        templateUrl: '/assets/partials/branch.html',
                        controller: BranchController
                    })
                    .otherwise({
                        redirectTo: '/branches'
                    })
        ])


}