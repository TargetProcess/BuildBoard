/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    angular.module('buildBoard', ['ngRoute','ui.bootstrap'])
        .service(BackendService.NAME, BackendService)
        .controller('branchesController', BranchesController)
        .controller(BranchLineController.NAME, BranchLineController)
        .filter('status', status)
        .filter('activeFilter', activeFilter)
        .filter('encode', encode)
        .filter('pullRequestStatus', pullRequestStatus)
        .directive('entityTitle', ()=>new EntityTitleDirective())
        .directive(EntityStateDirective.NAME, ()=>new EntityStateDirective())
        .directive(PanelDirective.NAME, ()=>new PanelDirective())
        .directive(PanelHeadingDirective.NAME, ()=>new PanelHeadingDirective())
        .directive(PanelBodyDirective.NAME, ()=>new PanelBodyDirective())
        .config(['$routeProvider',
            ($routeProvider:ng.route.IRouteProvider)=>
                $routeProvider
                    .when('/branches', {
                        templateUrl: '/assets/partials/main.html',
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