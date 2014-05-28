package exceptions

import components.MagicMergeResult

class NotFoundException(message:String) extends Exception(message)

class MergeFailedException(message:String, cause:Throwable=null) extends Exception(message, cause){
  def this(mergeResult:MagicMergeResult) = this(mergeResult.description)
}

