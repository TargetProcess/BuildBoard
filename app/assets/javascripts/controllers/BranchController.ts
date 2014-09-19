/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchDetailsScope extends IBranchScope {
        loadBuild(buildInfo:Build): void;
        getActivity(): ActivityEntry[];
    }

    export class BranchController {
        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME,
            ModelProvider.NAME
        ];

        constructor(private $scope:IBranchDetailsScope, $state:ng.ui.IStateService, backendService:BackendService) {



            this.$scope.branchName = $state.params['name'];
            this.$scope.closeView = ()=> {
                $state.go("list");
            };


            var defer:ng.IHttpPromise<Build> = null;
            $scope.loadBuild = buildInfo => {


                if (buildInfo.node == null && !defer) {
                    this.$scope.getBranch.then(branch=> {
                        defer = backendService.build(branch.data.name, buildInfo.number)
                            .success(build => {
                                buildInfo.node = build.node;
                                defer = null;
                            });

                    })

                }
            };
             /*
            $scope.getActivity = () => {

                var branch = this.$scope.getBranch();
                if (!branch)
                    return null;

                return branch.activity;
            }  */
        }
    }
}