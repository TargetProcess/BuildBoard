/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchDetailsScope extends IBranchScope {
        loadBuild(buildInfo:Build): void;
        getActivity(): ActivityEntry[];
        getBranch():Branch;
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

            modelProvider.getBranchWithActivities(this.$scope.branchName)
                .then(branch => {
                    this.$scope.getBranch = ()=> branch;
                    this.$scope.getActivity = ()=>branch.activity;
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