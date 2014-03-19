/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchDetailsScope extends IBranchScope {
        loadBuild(buildInfo:Build): void;
        getActivity(): ActivityEntry[];
    }

    export class BranchController extends BranchControllerBase {
        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME,
            ModelProvider.NAME
        ];

        constructor($scope:IBranchDetailsScope, $state:ng.ui.IStateService, backendService:BackendService, modelProvider:ModelProvider) {
            super($scope, backendService, modelProvider);

            this.$scope.branchName = $state.params['name'];
            this.$scope.closeView = ()=> {
                $state.go("list");
            };

            var defer = null;
            $scope.loadBuild = buildInfo => {
                if (buildInfo.node == null && !defer) {
                    defer = backendService.build(this.$scope.getBranch().name, buildInfo.number).success(build => {
                        buildInfo.node = build.node;
                        defer = null;
                    });
                }
            };

            $scope.getActivity = () => {
                var branch = this.$scope.getBranch();
                if (!branch)
                    return null;

                return branch.activity;
            }
        }
    }
}