package models

import org.kohsuke.github.{GHBranch, GitHub}
import collection.JavaConversions._

class GitHubRepository(accessToken: String) {
  private val github = GitHub.connectUsingOAuth(accessToken)
  private val repo = github.getRepository("TargetProcess/TP")
  def getBranches: Iterable[Branch] = {
    val branches: Iterable[GHBranch] = repo.getBranches.values

    branches.map(x => Branch.create(x.getName))
  }
}
