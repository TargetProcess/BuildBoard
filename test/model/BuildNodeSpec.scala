package model

import globals.Matchers._
import models.BuildNode
import org.specs2.mutable.Specification


class BuildNodeSpec extends Specification {

  "BuildNode" should {
    "return all childrens" in {

      def node(name: String, children: BuildNode*) = BuildNode(name, "", None, "", Nil, null, None, children = children.toList)


      val root = node("root",
        node("child1",
          node("gc1")
        ),
        node("child2",
          node("grandChild1"),
          node("grandChild2")
        )
      )


      root.allChildren.map(_.name) must beTheSeqSameAs(List("child1", "gc1", "child2", "grandChild1", "grandChild2"))

    }
  }
}