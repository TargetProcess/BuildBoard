/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class BranchesController {
        public static $inject = [
            '$scope',
            BackendService.NAME,
            '$q'
        ];

        constructor(private $scope:IBranchesScope, backendService:BackendService, q:ng.IQService) {
            this.$scope.setFilter = this.setFilter.bind(this);
            this.$scope.checkCurrentFilter = this.checkCurrentFilter.bind(this);

            this.$scope.getUserFilter = this.getUserFilter.bind(this);

            this.$scope.allBranchesFilter = new Filter(branch=>true);
            this.$scope.entityBranchesFilter = new Filter(branch=>!!branch.entity);

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


                this.$scope.loading = false;
            });

            /*.success((data:Branch[])=> {
             this.$scope.allBranches = data;
             }).error(()=> {
             this.$scope.loading = false;
             });

             backendService.
             */
        }

        private getUserFilter(userId:number) {
            return new Filter(branch=> {
                return branch.entity && _.any(branch.entity.assignments, assignemnt=>assignemnt.userId == userId);
            });
        }

        private setFilter(filter:IFilter) {
            this.$scope.currentFilter = filter;
        }

        private checkCurrentFilter(filter:IFilter) {
            return this.$scope.currentFilter == filter;
        }
    }


}