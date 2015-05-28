package models.github

import java.io.IOException

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.IGitHubConstants._
import org.eclipse.egit.github.core.service.DataService;


class ReferenceService(client: GitHubClient) extends DataService(client) {
  /**
   * Delete reference with given name
   *
   * @param repository
   * @param ref
   * @throws IOException
   */
  def deleteReference(repository: IRepositoryIdProvider, ref: String) = {
    val id: String = getId(repository)
    if (ref == null) throw new IllegalArgumentException("Name cannot be null")
    if (ref.length == 0) throw new IllegalArgumentException("Name cannot be empty")
    val uri: StringBuilder = new StringBuilder
    uri.append(SEGMENT_REPOS)
    uri.append('/').append(id)
    uri.append(SEGMENT_GIT)
    if (!ref.startsWith("refs/")) uri.append(SEGMENT_REFS)
    uri.append('/').append(ref)
    client.delete(uri.toString())
  }
}

