/// <reference path='_all.ts' />


module buildBoard {
    'use strict';

    var buildBoard = angular.module('buildBoard', ['ui.bootstrap'])
        .controller('branchesController', BranchesController)
        .controller('pullRequestController', PullRequestController)
        .filter('activeFilter', activeFilter)


}