/// <reference path="../_all.ts" />
module buildBoard {
    export class BuildStatusBadgeDirective implements ng.IDirective {
        static NAME = "buildStatusBadge";

        scope = {
            build: "=",
            name: "@",
            time: "@"
        };

        template = '<div ng-show="build()" ui-sref="list.branch({name:name})" class="global-status pointer {{ build().parsedStatus | status2Class }}">' +
            '<span class="status {{ build().parsedStatus | status2Class }}">' +
            '{{name}} {{time=="now" ? "is" : "was" }}  {{ build().parsedStatus | status2text }} {{time=="now" ? "since" : "at" }}' +
            ' {{ build().timestamp | friendlyDate }}' +
            '</span>' +
            '</div>';

        restrict = "E";
        replace = true;

    }


}