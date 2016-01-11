package buildboard2.components

import models.BuildRepositoryComponentImpl
import models.configuration.ConfigComponentImpl

trait DefaultComponent
  extends AccountRepositoryComponentImpl
  with Build2RepositoryComponentImpl
  with Job2RepositoryComponentImpl
  with Artifact2RepositoryComponentImpl
  with BuildRepositoryComponentImpl
  with ConfigComponentImpl

class DefaultRegistry extends DefaultComponent
