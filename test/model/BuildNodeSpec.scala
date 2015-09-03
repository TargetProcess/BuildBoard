package model

import globals.Matchers._
import models.BuildNode
import models.BuildStatus.{Ok, Failure}
import org.specs2.mutable.Specification


class BuildNodeSpec extends Specification {

  def node(name: String, status: String, isUnstable: Boolean = false, children: List[BuildNode] = Nil) = BuildNode(
    name = name,
    runName = "",
    status = Some(status),
    statusUrl = "",
    artifacts = Nil,
    timestamp = null,
    rerun = None,
    children = children,
    isUnstable = Some(isUnstable))


  def succ(name:String, children: BuildNode*) = node(name,"SUCCESS", children=children.toList)
  def fail(name:String, children: BuildNode*) = node(name,"FAILURE", children=children.toList)
  def failUnstable(name:String, children: BuildNode*) = node(name,"FAILURE", isUnstable = true, children=children.toList)

  "BuildNode" should {
    def n(name: String, children: BuildNode*) = node(name, "", isUnstable = false, children.toList)


    "return all children" in {
      val root = n("root",
        n("child1",
          n("gc1")
        ),
        n("child2",
          n("grandChild1"),
          n("grandChild2")
        )
      )
      root.allChildren.map(_.name) must beTheSeqSameAs(List("child1", "gc1", "child2", "grandChild1", "grandChild2"))
    }

    "BuildNode status" should {

      "pop up from children" in {
        val root = succ("Root", succ("Child", fail("Grandchild")))
        root.buildStatus must beTheSameAs(Failure)
      }

      "pop up from children but do not track unstable" in {
        val root = succ("Root", succ("Child", failUnstable("Grandchild")))
        root.buildStatus must beTheSameAs(Ok)
      }
    }
  }
}