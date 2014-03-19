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

                _.each(branches, (br:Branch)=> {
                    _.each(br.activity, a=> {
                        switch (a.activityType) {
                            case 'build':
                                var build:Build = a;
                                build.parsedStatus = StatusHelper.parseInfo(build.status, build.toggled);
                                break;
                        }
                    });
                    if (br.lastBuild) {
                        br.lastBuild.parsedStatus = StatusHelper.parseInfo(br.lastBuild.status, br.lastBuild.toggled);
                    }
                });

                this.branches = branches;
            });
        }

        public findBranch(branchName:string):Branch {
            return _.find(this.branches, b=>b.name == branchName);
        }
    }
}