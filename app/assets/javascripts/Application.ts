/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    var buildBoard = angular.module('buildBoard', [])
        .controller('branchesController', BranchesController)
        .controller('pullRequestController', PullRequestController)
        .filter('activeFilter', activeFilter)


}