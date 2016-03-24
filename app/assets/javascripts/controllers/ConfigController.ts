/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';
    /*
     export interface IBranchDetailsScope extends IBranchScope {
     loadBuild(buildInfo:Build): void;
     getActivity(): ActivityEntry[];
     getBranch():Branch;
     getBuildStatus(build:Build):Status;
     getGravatar(email:string):string;
     processCommitMessage(message:string):string;
     }
     */
    export class ConfigController {
        public static NAME = "ConfigController";

        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME
        ];

        constructor(private $scope:any, $state:ng.ui.IStateService,backendService:BackendService) {

            console.log('hello');

            $scope.obj = {
                data: {'hello': 'world'},
                options: {mode: 'tree'}
            };

            $scope.btnClick = ()=> {
                $scope.obj.options = 'code';
            }
        }
    }
}