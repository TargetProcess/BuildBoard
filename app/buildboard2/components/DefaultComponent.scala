package buildboard2.components

import models.BuildRepositoryComponentImpl
import models.configuration.ConfigComponentImpl
import models.jenkins.JenkinsServiceComponentImpl

trait DefaultComponent
  extends AccountRepositoryComponentImpl
  with Job2RepositoryComponentImpl
  with Artifact2RepositoryComponentImpl
  with BuildRepositoryComponentImpl
  with ConfigComponentImpl

class DefaultRegistry extends DefaultComponent
