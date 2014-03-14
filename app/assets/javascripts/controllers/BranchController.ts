/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchDetailsScope extends IBranchScope {
        loadBuild(buildInfo: BuildInfo): void;
    }

    export class BranchController extends BranchControllerBase {
        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME,
            ModelProvider.NAME,
        ];

        constructor(public $scope:IBranchDetailsScope, $state: ng.ui.IStateService, backendService: BackendService, modelProvider:ModelProvider) {
            super($scope, backendService);


            this.$scope.branchName = $state.params['name'];
            this.$scope.closeView = ()=> {
                $state.go("list");
            };

            this.$scope.loadBuild = (buildInfo: BuildInfo) => {
                if (buildInfo.build == null) {
                    backendService.build(this.$scope.branch.name, buildInfo.number).success(build => {
                        buildInfo.build = build;
                    });
                }
            };

            var buildsRequest = backendService.builds(this.$scope.branchName);

            modelProvider.findBranch(this.$scope.branchName).then(branch=>{
                this.$scope.branch = branch;

                this.loadPullRequestStatus(this.$scope.branch);
                buildsRequest.success(builds => {
                    this.$scope.builds = builds;
                    var lastBuild = _.first(builds);
                    this.$scope.branch.lastBuild = lastBuild;
                    if (lastBuild){
                        this.$scope.loadBuild(lastBuild);
                    }
                });
            });
        }
    }
}