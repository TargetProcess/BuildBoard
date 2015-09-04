/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchDetailsScope extends IBranchScope {
        loadBuild(buildInfo:Build): void;
        getActivity(): ActivityEntry[];
        getBranch():Branch;
        getBuildStatus(build:Build):Status;
        getGravatar(email:string):string;
        processCommitMessage(message:string):string;
    }

    export class BranchController {
        public static $inject = [
            '$scope',
            '$state',
            ModelProvider.NAME,
            BackendService.NAME
        ];

        constructor(private $scope:IBranchDetailsScope, $state:ng.ui.IStateService, modelProvider:ModelProvider, backendService:BackendService) {


            this.$scope.branchName = $state.params['name'];
            this.$scope.closeView = ()=> $state.go("list");
            this.$scope.getBuildStatus = StatusHelper.parse;

            this.$scope.getGravatar = _.memoize((email:string)=> email ? md5(email.toLowerCase().trim()) : '0');
            this.$scope.processCommitMessage = subject=>subject&&subject.replace(/Merge pull request #(\d+)/g,
                'Merge pull request <a href="https://github.com/TargetProcess/TP/pull/$1" target="_blank">#$1</a>');


            modelProvider.getBranchWithActivities(this.$scope.branchName)
                .then(branch => {
                    this.$scope.getBranch = ()=> branch;
                    this.$scope.getActivity = ()=> branch.activity;
                });


            var defer:ng.IHttpPromise<Build> = null;
            $scope.loadBuild = buildInfo => {
                if (buildInfo.node == null && !defer) {
                    defer = backendService.build(this.$scope.getBranch().name, buildInfo.number).success(build => {
                        buildInfo.node = build.node;
                        defer = null;
                    });
                }
            };
        }
    }
}