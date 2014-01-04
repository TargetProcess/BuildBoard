/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchesState {
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

        userFilter:any;
        branchesFilter:any;
    }


    export class BranchesController {
        public static $inject = [
            '$scope',
            '$state',
            BranchesService.NAME,
            LoggedUserService.NAME
        ];

        constructor(private $scope:IBranchesScope, $state:ng.ui.IStateService, branchesService:IBranchesService, private loggedUserService:LoggedUserService) {
            console.log($state);
            this.$scope.loading = true;

            this.$scope.userFilter = $state.params['user'] || 'all';
            this.$scope.branchesFilter = $state.params['branch'] || 'all';


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

                var counts = _.countBy(usersAndBranches, userAndBranch=>userAndBranch.user.userId);

                this.$scope.users = _.chain(usersAndBranches).unique(false, pair=>pair.user.userId)
                    .map(x=> {
                        var user = x.user;
                        user.count = counts[user.userId];
                        return user;

                    }).value();

                this.$scope.branches = this.filter(branches, this.$scope.userFilter, this.$scope.branchesFilter);

                this.$scope.countByUser = (userFilter:string)=>this.filter(branches, userFilter, "all").length;
                this.$scope.countByBranch = (branchFilter:string)=>this.filter(branches, this.$scope.userFilter, branchFilter).length;

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