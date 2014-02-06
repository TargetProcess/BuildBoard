/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface ITestCaseScope extends ng.IScope {
        testCase: TestCase;
        closeView():void;
    }

    export class TestCaseController {
        public static NAME = "TestCaseController";

        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME
        ];

        constructor(private $scope:ITestCaseScope, $state:ng.ui.IStateService, backendService:BackendService) {
            backendService.testCase($state.params['name'], $state.params['build'], $state.params['part'], $state.params['run'], $state.params['test']).success(testCase => {
                this.$scope.testCase = testCase;
            });

            this.$scope.closeView = () => {
                $state.go("list.branch.run");
            };
        }
    }
}