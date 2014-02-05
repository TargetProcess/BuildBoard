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
            '$state'
        ];

        constructor(private $scope:ITestCaseScope, $state:ng.ui.IStateService) {
            console.log('test case');
            var testName = $state.params["testCase"];
            var parentScope = <IArtifactsScope>$scope.$parent;
            this.$scope.testCase = _.chain(parentScope.testCasePackages).map(p => p.testCases).flatten().filter(tc => tc.name == testName).head().value();

            this.$scope.closeView = ()=> {
                $state.go("list.branch.testCasePackage");
            };
        }
    }
}