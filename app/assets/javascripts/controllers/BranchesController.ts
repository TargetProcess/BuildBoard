/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchesRouteParams extends ng.IRouteParamsService {
        filter:string;
    }

    export interface IBranchesScope extends ng.IScope {
        allBranches:Branch[];
        users:User[];

        getBuildClass(branch:Branch);
        count(filterName:string):number;

        loading:boolean;
    }


    export class BranchesController {
        public static $inject = [
            '$scope',
            '$routeParams',
            BranchesService.NAME
        ];

        constructor(private $scope:IBranchesScope, $routeParams:IBranchesRouteParams, branchesService:IBranchesService) {
            this.$scope.loading = true;

            branchesService.allBranches.then(branches=> {
                var usersAndBranches = _.chain(branches)
                    .filter(branch=>!!branch.entity)
                    .map(branch=>
                        _.map(branch.entity.assignments, user=> {
                            return {user: user, branch: branch};
                        })
                )
                    .flatten()
                    .value();


                var counts = _.countBy(usersAndBranches, userAndBranch=>userAndBranch.user.userId);

                this.$scope.users = _.chain(usersAndBranches).unique(false, pair=>pair.user.userId)
                    .map(x=> {
                        var user = x.user;
                        user.count = counts[user.userId];
                        return user;

                    }).value();

                this.$scope.allBranches = this.filter(branches, $routeParams.filter);

                this.$scope.count = (filterName:string)=>this.filter(branches, filterName).length;


            }).then(x=> {
                    this.$scope.loading = false;
                });
        }


        filter(list:Branch[], filterName:string):Branch[] {
            var userId = parseInt(filterName, 10);
            if (!isNaN(userId)) {
                return _.filter(list, branch=>branch.entity && _.any(branch.entity.assignments, assignment=>assignment.userId == userId));
            }
            if (filterName == "entity") {
                return _.filter(list, branch=>branch.entity);
            }
            if (filterName == "closed") {
                return _.filter(list, branch=>branch.entity && branch.entity.state.isClosed);
            }
            return list;
        }


    }


}