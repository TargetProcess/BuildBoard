/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    angular.module('buildBoard', ['ui.router', 'ui.bootstrap'])
        .service(BackendService.NAME, BackendService)
        .service(BranchesService.NAME, BranchesService)
        .service(LoggedUserService.NAME, LoggedUserService)
        .controller('branchesController', BranchesController)
        .controller(BranchLineController.NAME, BranchLineController)
        .controller(HeaderController.NAME, HeaderController)
        .controller(RunController.NAME, RunController)
        .controller(TestCaseController.NAME, TestCaseController)
        .filter('status', status)
        .filter('activeFilter', activeFilter)
        .filter('encode', encode)
        .filter('pullRequestStatus', pullRequestStatus)
        .filter('duration', duration)
        .directive('entityTitle', ()=>new EntityTitleDirective())
        .directive(EntityStateDirective.NAME, ()=>new EntityStateDirective())
        .directive(PanelDirective.NAME, ()=>new PanelDirective())
        .directive(PanelHeadingDirective.NAME, ()=>new PanelHeadingDirective())
        .directive(PanelBodyDirective.NAME, ()=>new PanelBodyDirective())
        .directive(BuildStatusDirective.NAME, ()=>new BuildStatusDirective())
        .config(($stateProvider:ng.ui.IStateProvider, $urlRouterProvider:ng.ui.IUrlRouterProvider)=> {

            $urlRouterProvider.otherwise("/list?user=my&branch=all");

            $stateProvider.state('list', {
                url: "/list?user&branch",
                templateUrl: "/assets/partials/main.html",
                controller: BranchesController
            })
                .state('list.branch', {
                    url: "/branch?name",
                    templateUrl: "/assets/partials/branch.html",
                    controller: BranchController
                })
                .state('list.branch.run', {
                    url: "/run?build&part&run",
                    templateUrl: "/assets/partials/run.html",
                    controller: RunController
                })
                .state('list.branch.run.testCase', {
                    params: ["name", "user", "branch", "build", "part", "run", "testCase"],
                    templateUrl: "/assets/partials/testCase.html",
                    controller: TestCaseController
                })

        }).run(function ($rootScope, $state) {
            $rootScope.$state = $state;
        })

}