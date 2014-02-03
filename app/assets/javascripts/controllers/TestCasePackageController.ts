/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface ITestCasePackageScope extends ng.IScope {
        file: string;
        closeView():void;
    }

    export class TestCasePackageController {
        public static NAME = "TestCasePackageController";

        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME
        ];

        constructor(private $scope:ITestCasePackageScope, $state:ng.ui.IStateService) {
            this.$scope.file = $state.params['file'];
            let branchName = $state.params['name'];

            this.$scope.closeView = ()=> {
                $state.go("list.branch({name:'" + branchName +"'})");
            };
        }
    }
}