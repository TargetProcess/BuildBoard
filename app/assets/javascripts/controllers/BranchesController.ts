/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class BranchesController {
        public static $inject = [
            '$scope',
            '$http',
            ServerRoutesService.NAME
        ];

        constructor(private $scope:IBranchesScope, $http:ng.IHttpService, serverRoutesService:ServerRoutesService) {
            this.$scope.setFilter = this.setFilter.bind(this);
            this.$scope.checkCurrentFilter = this.checkCurrentFilter.bind(this);

            this.$scope.getUserFilter = this.getUserFilter.bind(this);

            this.$scope.allBranchesFilter = new Filter(branch=>true);
            this.$scope.entityBranchesFilter = new Filter(branch=>!!branch.entity);

            this.$scope.loading = true;

            $http.get(serverRoutesService.branches()).success((data:Branch[])=> {
                this.$scope.allBranches = data;
                this.$scope.users = _.chain(data)
                    .filter(branch=>!!branch.entity)
                    .map(branch=>branch.entity.assignments)
                    .flatten()
                    .unique(false, user=>user.userId)
                    .value();

                this.$scope.loading = false;
            }).error(()=> {
                    this.$scope.loading = false;
                });


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