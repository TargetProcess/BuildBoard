/// <reference path='../_all.ts' />

module buildBoard {

    export class BranchesService {
        public static NAME = "branchesService";

        public static $inject = [
            BackendService.NAME
        ];

        public allBranches:ng.IPromise<Branch[]>;

        constructor(private $backendService:BackendService) {
            this.allBranches = this.$backendService.branches().then(branches => branches.data);
        }

        public getActivities(branchName):ng.IPromise<ActivityEntry[]> {
            return this.$backendService.getActivities(branchName).then(x => x.data);
        }
    }


}