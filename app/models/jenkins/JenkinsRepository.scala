package models.jenkins

import com.offbytwo.jenkins.JenkinsServer
import java.net.URI
import collection.JavaConversions._
import com.offbytwo.jenkins.model.{Build, Job}
import scala.collection.mutable.Map
import models.{NightBuild, PullRequestBuild}
import scala.collection.mutable

object JenkinsRepository {
  val jenkins = new JenkinsServer(new URI(JenkinsApplication.url))
  def getBuilds(branches: Iterable[String]) = {
    val jobs: Map[String, Job] = jenkins.getJobs
    val prJob = jenkins.getJob("buildpullrequest")
    val prBuilds: Iterable[Build] = prJob.getBuilds
    val prActions = prBuilds.map(x => {
      val actions: Iterable[Any] = x.details.getActions
      (x, actions)
    })
    .map(x => {
      val actions: Iterable[Any] = x._2
      val actions1 = actions.map(y => {
        val ac: Map[String, Any] = y.asInstanceOf[java.util.LinkedHashMap[String, Any]]
        ac
      })
      (x._1, actions1)
    })


    val lastBuilds = jobs.map(j => {
      val res = j._1 match {
        case s if s == "buildpullrequest" => {
          val builds: Iterable[Build] = j._2.details().getBuilds
          val b = builds.map(x => {
            val list: Iterable[Any] = x.details.getActions

            val actions = list.map(l => {
              val hash: Map[String, Any] = l.asInstanceOf[java.util.LinkedHashMap[String, Any]]
              val q = hash.map(h => {
                val wl: Iterable[Any] = h._2.asInstanceOf[java.util.List[Any]]
                val wll = wl.map(r => {
                  val w: Map[String, Any] = r.asInstanceOf[java.util.LinkedHashMap[String, Any]]
                  w
                })
                wll
              })
              q
            })
            actions
          })
          val lastBuild = j._2.details.getLastBuild
          val lastBuildDetails = lastBuild.details
          PullRequestBuild(lastBuild.getNumber, lastBuildDetails.getFullDisplayName, lastBuildDetails.getUrl)
        }
        case s if s == "triggernigthlybuild" => {
          val lastBuild = j._2.details.getLastBuild
          val lastBuildDetails = lastBuild.details
          NightBuild(lastBuild.getNumber, lastBuildDetails.getFullDisplayName, lastBuildDetails.getUrl)
        }
        case _ => null
      }

      res
    })
    .filter(x => x != null)

    lastBuilds
  }
}
