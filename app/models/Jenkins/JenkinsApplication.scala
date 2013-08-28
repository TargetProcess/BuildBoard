package models.jenkins

object JenkinsApplication {
  val url = "http://jenkinsmaster-hv:8080"
  def getJobInfo(job: String) = {
    s"$url/job/$job/api/xml?depth=2&tree=downstreamProjects[name],builds[url,runs[result,url,number],actions[parameters[name,value],causes[upstreamBuild,upstreamProject],lastBuiltRevision[branch[name]]],number,result,timestamp,changeSet[items[author[fullName],msg]],artifacts[fileName,relativePath]]"
  }
}
