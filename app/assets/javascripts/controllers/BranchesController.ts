/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class BranchesController {
        public static $inject = [
            '$scope',
            '$http',
            '$window'
        ];


        constructor(private $scope:IBranchesScope, private $http:ng.IHttpService, private $window:IBuildBoardWindow, private filterFilter) {
            this.$scope.predicate = 'state';
            this.$scope.isShowingAll = true;
            this.$scope.loading = true;
            $http.get($window.jsRoutes.controllers.Application.branches().absoluteURL()).success((data:Branch[])=> {
                this.$scope.allBranches = data;
                this.$scope.entityBranches = this.filterBranchesByEntity(data);
                this.$scope.entityBranchesLength = $scope.entityBranches.length;
                this.$scope.branches = data;
                this.$scope.users = _.chain(data)
                    .filter(branch=>!!branch.entity)
                    .map(branch=>branch.entity.assignments)
                    .flatten()
                    .unique(false, user=>user.userId)
                    .value();

                $scope.branchCount = id=>this.filterBranchesById($scope.allBranches, id).length;
                this.$scope.loading = false;
            }).error(()=> {
                    this.$scope.loading = false;
                });

            this.$scope.filterBranch = (id:number)=> {
                this.$scope.isShowingAll = false;
                this.$scope.isShowingEntity = false;
                this.$scope.isShowingId = id;
                this.$scope.branches = this.filterBranchesById($scope.allBranches, id);
            };

            this.$scope.resetFilterBranch = ()=> {
                this.$scope.isShowingId = null;
                this.$scope.isShowingAll = true;
                this.$scope.isShowingEntity = false;
                this.$scope.branches = $scope.allBranches;
            };
            this.$scope.filterOnlyEntityBranch = ()=> {
                this.$scope.isShowingId = null;
                this.$scope.isShowingAll = false;
                this.$scope.isShowingEntity = true;
                this.$scope.branches = $scope.entityBranches;
            }
        }

        private filterBranchesById(branches:Branch[], id:number) {
            return _.filter(branches, branch => branch.entity && _.any(branch.entity.assignments, assignment=>assignment.userId == id));
        }

        private filterBranchesByEntity(branches:Branch[]) {
            return _.filter(branches, branch=>branch.entity);
        }

        public filterBranch(id:number) {
            this.$scope.isShowingAll = false;
            this.$scope.isShowingEntity = false;
            this.$scope.isShowingId = id;
            this.$scope.branches = this.filterBranchesById(this.$scope.allBranches, id);
        }
    }
}