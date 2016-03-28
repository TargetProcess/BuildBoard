/// <reference path='_all.ts' />
/// <reference path='services/BranchesService.ts'/>
/// <reference path='directives/BuildStatusBadge'/>
/// <reference path='directives/MergeButtonDirective.ts'/>

module buildBoard {
    'use strict';

    angular.module('buildBoard', ['ui.router', 'checklist-model', 'btford.markdown', 'ng.jsoneditor'])
        .service(HttpServiceNotificationDecorator.NAME, HttpServiceNotificationDecorator)
        .service(BackendService.NAME, BackendService)
        .service(LoggedUserService.NAME, LoggedUserService)
        .service(BranchesService.NAME, BranchesService)
        .service(ModelProvider.NAME, ModelProvider)
        .controller(StatusController.NAME, StatusController)
        .controller('branchesController', BranchesController)
        .controller(BranchLineController.NAME, BranchLineController)
        .controller(HeaderController.NAME, HeaderController)
        .controller(RunController.NAME, RunController)
        .controller(TestCaseController.NAME, TestCaseController)
        .controller(ConfigController.NAME, ConfigController)
        .filter('status2Class', status2Class)
        .filter('parseBuildNodeStatus', parseBuildNodeStatus)
        .filter('parseTestCaseStatus', parseTestCaseStatus)
        .filter('status2text', status2text)
        .filter('activeFilter', activeFilter)
        .filter('encode', encode)
        .filter('pullRequestStatus', pullRequestStatus)
        .filter('duration', duration)
        .filter('suppressZero', suppressZero)
        .filter('friendlyDate', friendlyDate)
        .directive('entityTitle', ()=>new EntityTitleDirective())
        .directive(EntityStateDirective.NAME, ()=>new EntityStateDirective())
        .directive(BuildStatusDirective.NAME, ()=>new BuildStatusDirective())
        .directive(BuildStatusBadgeDirective.NAME, ()=>new BuildStatusBadgeDirective())
        .directive(MergeButtonDirective.NAME, ()=>new MergeButtonDirective())
        .config(($stateProvider:ng.ui.IStateProvider, $urlRouterProvider:ng.ui.IUrlRouterProvider)=> {

            $urlRouterProvider.otherwise("/list?user=my&branch=all");

            $stateProvider
                .state('config', {
                    url: "/config",
                    templateUrl:"/assets/partials/config.html",
                    controller: ConfigController
                }).state('list', {
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
                    url: "/testCase?test",
                    templateUrl: "/assets/partials/testCase.html",
                    controller: TestCaseController
                })


        }).run(function ($rootScope, $state) {
        $rootScope.$state = $state;
    })

}