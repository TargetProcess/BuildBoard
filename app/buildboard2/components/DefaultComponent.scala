package buildboard2.components

import models.BuildRepositoryComponentImpl

trait DefaultComponent
  extends AccountRepositoryComponentImpl
  with Build2RepositoryComponentImpl
  with Job2RepositoryComponentImpl
  with BuildRepositoryComponentImpl

class DefaultRegistry extends DefaultComponent
