import scala.actors.Actor
import java.io.FileOutputStream
import java.io.IOException

/**
 * Class that represents the worker Actor
 * @author Zhishen Wen
 * @version Nov 24, 2013
 * CIS 554
 */
class Worker(root: String, curr: String, dir: String, 
             limit: Int, caller: Actor) extends Actor {
  
  /** defines action for this Actor */
  def act = {
    caller ! Signal.New
    run()
    caller ! Signal.Die
  }
  
  /**
   * Runs this worker Actor.
   * This method handles all the necessary logics for
   * a worker to parse the web page, fetch information,
   * and interact with the front proxy.
   */
  def run() {
    var bytes: Array[Byte] = Array()
    getFromUrl(root + curr) match {
      case None => return
      case Some(b) => bytes = b
    }
    saveToFile(dir + curr, bytes)
    
    var ignCnt = 0
    var lmtCnt = 0
    val content = new String(bytes).replaceAll("[\n\r]", "")
    val links = "(<img.*?(src|SRC)=[\"|'](.*?)[\"|'])|(<a.*?(href|HREF)=[\"|'](.*?)[\"|'])".r findAllIn content
    for (l <- links) {
      var link = l.trim
      if (link.contains("href=")) 
        link = link.substring(link.indexOf("href=") + 6, link.length - 1)
      else if (link.contains("HREF=")) 
        link = link.substring(link.indexOf("HREF=") + 6, link.length - 1)
      else if (link.contains("src=")) 
        link = link.substring(link.indexOf("src=") + 5, link.length - 1)
      else if (link.contains("SRC=")) 
        link = link.substring(link.indexOf("SRC=") + 5, link.length - 1)
      link match {
        
        // ignores improper info
        case s if s.matches("(^(https?:|\\.\\.|/).*)|(.*#.*)") => ignCnt += 1
        
        // creates a new worker Actor to deal with new web page, 
        // or reports total number of pages beyond the depth limit
        case s if s.matches(".*\\.html?$") =>
          if (limit > 0) new Worker(root, s, dir, limit - 1, caller).start
          else lmtCnt += 1
        
        // downloads all supported images
        case s if s.matches(".*\\.(gif|jpe?g|png)$") =>
          getFromUrl(root + s) match {
            case None => /* ignore */
            case Some(img) => saveToFile(dir + s, img)
          }
        
        // ignores improper info
        case _ => ignCnt += 1
      }
    }
    caller ! (Signal.Ignore, ignCnt)
    if (limit == 0) caller ! (Signal.Beyond, lmtCnt)
  }
  
  /**
   * Gets a byte array from this given URL.
   * @return An Option[Array[Byte]]
   */
  def getFromUrl(from: String): Option[Array[Byte]] = {
    var in: java.io.InputStream = null
    try {
      val url = new java.net.URL(from)
      in = url.openStream() 
    }
    catch { 
      case e: IOException =>
        println("(Depth "+limit+") Fetching: " + from + " [FAILED]")
        caller ! (Signal.Fail, from)
        return None
    }
    val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
    println("(Depth "+limit+") Fetching: " + from + " [SUCCESS]")
    caller ! (Signal.Success, from)
    in.close()
    Some(bytes)
  }
  
  /**
   * Writes this byte array to the file.
   */
  def saveToFile(to: String, bytes: Array[Byte]) {
    val file = new java.io.File(to)
    if (!file.exists()) file.getParentFile().mkdirs()
    val out = new FileOutputStream(file)
    out.write(bytes)
    out.close()
  }
}