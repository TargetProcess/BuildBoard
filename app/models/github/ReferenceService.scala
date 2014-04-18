package models.github

import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.client.IGitHubConstants._

class ReferenceService(client: GitHubClient) extends DataService(client) {
  /**
   * Delete reference with given name
   *
   * @param repository
   * @param name
   * @throws IOException
   */
  def deleteReference(repository: IRepositoryIdProvider, name: String) = {
    val id: String = getId(repository)
    if (name == null) throw new IllegalArgumentException("Name cannot be null")
    if (name.length == 0) throw new IllegalArgumentException("Name cannot be empty")
    val uri: StringBuilder = new StringBuilder
    uri.append(SEGMENT_REPOS)
    uri.append('/').append(id)
    uri.append(SEGMENT_GIT)
    if (!name.startsWith("refs/")) uri.append(SEGMENT_REFS)
    uri.append('/').append(name)
    client.delete(uri.toString())
  }
}
