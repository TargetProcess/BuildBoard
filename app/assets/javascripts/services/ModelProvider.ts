/// <reference path='../_all.ts' />
/// <reference path='BranchesService.ts'/>
module buildBoard {

    export class ModelProvider {
        public static NAME = "modelProvider";

        public static $inject = [
            BranchesService.NAME
        ];

        public branches:Branch[];

        constructor($branchesService:BranchesService) {
            $branchesService.allBranches.then(branches=> {
                this.branches = branches;
            });
        }

        public findBranch(branchName:string):Branch {
            return _.find(this.branches, b=>b.name == branchName);
        }
    }
}