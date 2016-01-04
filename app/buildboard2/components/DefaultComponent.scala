package buildboard2.components

import models.BuildRepositoryComponentImpl

trait DefaultComponent
  extends AccountRepositoryComponentImpl
  with BuildRepositoryComponentImpl

class DefaultRegistry extends DefaultComponent
