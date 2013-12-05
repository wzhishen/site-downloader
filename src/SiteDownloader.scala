import scala.io.Source
import scala.actors.Actor
import Actor._
import java.io.IOException
import java.io.FileOutputStream
import java.io.File

/**
 * Main class to launch the program.
 * @author Zhishen Wen
 * @version Nov 24, 2013
 * CIS 554
 */
object SiteDownloader {
  
  def main(args: Array[String]) {
    val gui = new GUI
    
    // asks for a URL
    var root = ""
    var curr = ""
    var url = gui.showInputDialog("Please enter a complete valid URL:")
    if (!url.startsWith("http")) url = "http://" + url
    if (url.endsWith("/")) {
      root = url
      curr = "index.html"
    }
    else {
      root = url.substring(0, url.lastIndexOf("/") + 1)
      curr = url.substring(url.lastIndexOf("/") + 1)
    }

    // asks for a local directory
    gui.showInfoDialog("Information", "Please choose a directory.")
    val dir = gui.showDirChooser() + "/"
    
    // asks for depth limit
    val limit = gui.showInputNumberDialog("Please enter a number for the depth limit (0, 1, 2...):")
    
    gui.buildProcessGUI()
    
    var linksSuccess = List[String]()
    var linksFailed = List[String]()
    var ignCnt = 0
    var lmtCnt = 0
    
    var aliveActorCnt = 0
    val caller = self
    new Worker(root, curr, dir, limit, caller).start
    do {
      receive {
        case (Signal.Success, url: String) => linksSuccess = url :: linksSuccess
        case (Signal.Fail,    url: String) => linksFailed = url :: linksFailed
        case (Signal.Ignore,  cnt: Int)    => ignCnt += cnt
        case (Signal.Beyond,  cnt: Int)    => lmtCnt += cnt
        case Signal.New                    => aliveActorCnt += 1
        case Signal.Die                    => aliveActorCnt -= 1
      }
    } while(aliveActorCnt > 0)
    
    val linksSuccessSet = linksSuccess.toSet
    val linksFailedSet = linksFailed.toSet
    println("\n--------------------------------------------------------")
    println("All tasks done.")
    println("Total number of successful URLs: " + linksSuccessSet.size)
    println("Total number of unsuccessful URLs: " + linksFailedSet.size)
    println("Total number of URLs ignored because inappropriate: " + ignCnt)
    println("Total number of URLs beyond the depth limit: " + lmtCnt)
    
    gui.buildResultGUI(dir, linksSuccessSet, linksFailedSet, ignCnt, lmtCnt)
  }
}

/**
 * Enumeration that represents actor signals.
 */
object Signal extends Enumeration {
  type Signal = Value
  val Success, Fail, Ignore, Beyond, New, Die = Value
}