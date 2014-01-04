/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    angular.module('buildBoard', ['ui.router','ui.bootstrap'])
        .service(BackendService.NAME, BackendService)
        .service(BranchesService.NAME, BranchesService)
        .service(LoggedUserService.NAME, LoggedUserService)
        .controller('branchesController', BranchesController)
        .controller(BranchLineController.NAME, BranchLineController)
        .controller(HeaderController.NAME, HeaderController)
        .filter('status', status)
        .filter('activeFilter', activeFilter)
        .filter('encode', encode)
        .filter('pullRequestStatus', pullRequestStatus)
        .directive('entityTitle', ()=>new EntityTitleDirective())
        .directive(EntityStateDirective.NAME, ()=>new EntityStateDirective())
        .directive(PanelDirective.NAME, ()=>new PanelDirective())
        .directive(PanelHeadingDirective.NAME, ()=>new PanelHeadingDirective())
        .directive(PanelBodyDirective.NAME, ()=>new PanelBodyDirective())
        .config(($stateProvider:ng.ui.IStateProvider, $urlRouterProvider:ng.ui.IUrlRouterProvider)=>{

            $urlRouterProvider.otherwise("/list");

            $stateProvider.state('list', {
                url: "/list?user&branch",
                templateUrl: "/assets/partials/main.html",
                controller: BranchesController
            })
                .state('list.branch', {
                    url: "/branch?name",
                    templateUrl: "/assets/partials/branch.html",
                    controller:BranchController
                })

        })

}