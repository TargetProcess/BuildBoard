/// <reference path='../_all.ts' />
module buildBoard {

    export class BranchesService {
        public static NAME = "branchesService";

        public static $inject = [
            BackendService.NAME
        ];

        public allBranches:ng.IPromise<Branch[]>;
        public allBranchesWithLastBuilds:ng.IPromise<IMap<Build>>;

        constructor(private $backendService:BackendService) {
            this.$backendService = $backendService;
            this.allBranches = this.$backendService.branches().then(branches => branches.data);
            this.allBranchesWithLastBuilds = this.$backendService.lastBuilds().then(lastBuilds => lastBuilds.data);
        }
    }
}