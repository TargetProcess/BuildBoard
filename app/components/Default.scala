package components

import models.github.GithubRepositoryComponentImpl
import models.services.BranchServiceComponentImpl
import models.tp.TargetprocessComponentImpl
import models.jenkins.JenkinsRepositoryComponentImpl

trait Default
  extends AuthInfoProviderComponent
  with GithubRepositoryComponentImpl
  with BranchServiceComponentImpl
  with TargetprocessComponentImpl
  with JenkinsRepositoryComponentImpl
