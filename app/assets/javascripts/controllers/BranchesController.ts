/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchesRouteParams extends ng.IRouteParamsService {
        userFilter:string;
        branchesFilter:string;
    }

    export interface IBranchesScope extends ng.IScope {
        branches:Branch[];
        users:User[];

        getBuildClass(branch:Branch);
        countByUser(userFilter:string):number;
        countByBranch(branchFilter:string):number;

        loading:boolean;

        isFilterActive(name:string, value:any);

        getRoute(name:string, value:any):string;
    }


    export class BranchesController {
        public static $inject = [
            '$scope',
            '$routeParams',
            BranchesService.NAME,
            LoggedUserService.NAME
        ];

        constructor(private $scope:IBranchesScope, private $routeParams:IBranchesRouteParams, branchesService:IBranchesService, private loggedUserService:LoggedUserService) {
            this.$scope.loading = true;

            this.$scope.isFilterActive = (name:string, value:any)=>$routeParams[name] == value;
            this.$scope.getRoute = (name:string, value:any)=> {
                var routeValues = {
                    userFilter: $routeParams.userFilter,
                    branchesFilter: $routeParams.userFilter
                };
                routeValues[name] = value;

                var params = _.chain(routeValues)
                    .pairs()
                    .map(values=>values[0] + '=' + values[1])
                    .value()
                    .join('&');

                return "#/branchList?" + params;

            };

            branchesService.allBranches.then((branches:Branch[])=> {
                var usersAndBranches = _.chain(branches)
                    .filter(branch=>!!branch.entity)
                    .map((branch:Branch) =>
                        _.map(branch.entity.assignments, user=> {
                            return {user: user, branch: branch};
                        })
                )
                    .flatten()
                    .value();

                $routeParams.branchesFilter = $routeParams.branchesFilter || 'all';
                $routeParams.userFilter = $routeParams.userFilter || 'all';


                var counts = _.countBy(usersAndBranches, userAndBranch=>userAndBranch.user.userId);

                this.$scope.users = _.chain(usersAndBranches).unique(false, pair=>pair.user.userId)
                    .map(x=> {
                        var user = x.user;
                        user.count = counts[user.userId];
                        return user;

                    }).value();

                this.$scope.branches = this.filter(branches, $routeParams.userFilter, $routeParams.branchesFilter);

                this.$scope.countByUser = (userFilter:string)=>this.filter(branches, userFilter, "all").length;
                this.$scope.countByBranch = (branchFilter:string)=>this.filter(branches, $routeParams.userFilter, branchFilter).length;

            }).then(x=> {
                    this.$scope.loading = false;
                });
        }


        filter(list:Branch[], userFilter:string, branchFilter:string):Branch[] {

            var userPredicate;

            var userId = userFilter == "my" ? this.loggedUserService.getLoggedUser().userId : parseInt(userFilter, 10);
            if (!isNaN(userId)) {
                userPredicate = branch=>branch.entity && _.any(branch.entity.assignments, assignment=>assignment.userId == userId);
            }
            else {
                userPredicate = branch=>true;
            }

            var branchPredicate;
            if (branchFilter == "entity") {
                branchPredicate = branch=>branch.entity;
            } else if (branchFilter == "closed") {
                branchPredicate = (branch:Branch)=>branch.entity && branch.entity.state.isFinal;
            } else {
                branchPredicate = branch=>true;
            }

            return _.filter(list, branch=>userPredicate(branch) && branchPredicate(branch));
        }


    }


}