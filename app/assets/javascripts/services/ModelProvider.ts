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

                _.each(branches, (br:Branch)=>{
                    _.each(br.activity, a=>{
                        if (a.status){
                            a.parsedStatus = StatusHelper.parseInfo(a.status, a.toggled);
                        }
                    });
                    if (br.lastBuild) {
                        br.lastBuild.parsedStatus = StatusHelper.parseInfo(br.lastBuild.status, br.lastBuild.toggled);
                    }
                });


                console.log(branches);
                this.branches = branches;
            });
        }

        public findBranch(branchName:string):Branch {
            return _.find(this.branches, b=>b.name == branchName);
        }
    }
}