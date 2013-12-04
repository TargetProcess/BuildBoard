/// <reference path="../_all.ts" />
module buildBoard {
    export class PanelDirective implements ng.IDirective {
        static NAME = "panel";


        restrict = 'E';

        scope = {
            state: "@"
        };

        template = [
            '<div class="panel panel-{{ state }}" ng-transclude>',
            '</div>'
        ].join("");

        compile(element, attrs) {
            if (!attrs.state) {
                attrs.state = 'default'
            }
        }

        replace = true;
        transclude = true;
    }

    export class PanelHeadingDirective implements ng.IDirective {
        static NAME = "panelHeading";

        restrict = 'E';

        require = "panel";

        transclude = true;
        template = [
            '<div class="panel-heading" ng-transclude></div>'
        ].join("");
        replace = true;
    }

    export class PanelBodyDirective implements ng.IDirective {
        static NAME = "panelBody";

        restrict = 'E';

        require = "panel";

        transclude = true;
        template = [
            '<div class="panel-body" ng-transclude></div>'
        ].join("");
        replace = true;
    }
}