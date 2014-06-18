package model

import org.specs2.mutable.Specification
import models.BuildNode
import org.joda.time.DateTime

class BuildNodeSpec
  extends Specification {


  def genNode(i: Int, value: List[BuildNode] = Nil): BuildNode = {
    BuildNode("Node #" + i, i.toString, None, null, Nil, DateTime.now(), children = value)
  }

  "Build node" should {
    "return leaf nodes" in {

      val node = genNode(1, List(
        genNode(2,
          List(genNode(3), genNode(4), genNode(5)))
        , genNode(6)
      )
      )

      node.getLeafNodes.map(_.runName.toInt) must be_==(List(3, 4, 5, 6))

    }
  }


}