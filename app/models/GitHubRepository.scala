package models

import org.kohsuke.github.{GHBranch, GitHub}
import collection.JavaConversions._

class GitHubRepository(val accessToken: String) {
  def getBranches: Iterable[Branch] = {
    val github = GitHub.connectUsingOAuth(accessToken)
    val repo = github.getRepository("TargetProcess/TP")
    val branches: Iterable[GHBranch] = repo.getBranches.values

    branches.map(x => Branch(x.getName))
  }
}
