/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class BranchesController {
        public static $inject = [
            '$scope',
            '$routeParams',
            BackendService.NAME,
            '$q'
        ];

        constructor(private $scope:IBranchesScope, $routeParams:IBranchRouteParams, backendService:BackendService, q:ng.IQService) {

            this.$scope.loading = true;

            var branches = backendService.branches();
            var builds = backendService.buildsPerBuild();

            var promises:ng.IPromise<any>[] = [branches, builds];
            var allResult = q.all(promises);
            allResult.then((x:ng.IHttpPromiseCallbackArg<any>[])=> {
                    var branchesResult:Branch[] = x[0].data;
                    var buildsResult:{[branch:string]:Build
                    } = x[1].data;

                    _.chain(buildsResult)
                        .keys()
                        .each(key=> {
                            var match = key.match(/origin\/(?:pr\/(\d*)\/merge|(.+))/i);
                            if (match != null) {
                                var branch = _.find(branchesResult, x=> (match[2] && x.name.toLowerCase() == match[2].toLowerCase()) || (x.pullRequest && x.pullRequest.id == match[1]));
                                if (branch) {
                                    if (!branch.lastBuild || branch.lastBuild.timeStamp < buildsResult[key].timeStamp) {
                                        branch.lastBuild = buildsResult[key];
                                    }
                                }
                            }
                        });


                    this.$scope.allBranches = branchesResult;
                    this.$scope.users = _.chain(branchesResult)
                        .filter(branch=>!!branch.entity)
                        .map(branch=>branch.entity.assignments)
                        .flatten()
                        .unique(false, user=>user.userId)
                        .value();

                }
            ).then(x=> {
                    this.$scope.loading = false;
                });
        }
    }


}