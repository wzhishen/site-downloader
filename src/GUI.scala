import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Dialog.Result.Ok
import scala.swing.FileChooser
import scala.swing.FileChooser.Result.Approve
import scala.swing.FileChooser.Result.Cancel
import scala.swing.FileChooser.Result.Error
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.Swing
import scala.swing.TextArea
import scala.swing.event.ButtonClicked
import java.awt.Dimension
import java.awt.Font
import java.awt.Color

/**
 * Class that builds the GUI.
 * @author Zhishen Wen
 * @version Nov 24, 2013
 * CIS 554
 */
class GUI extends MainFrame {
  
  /**
   * Builds process GUI.
   */
  def buildProcessGUI() {
    title = "Site Downloader Running..."
    contents = new FlowPanel {
      contents += new Label {
        text = "Site downloader is now processing... Please wait."
        font = new Font("Arial", 28, 20)
      }
      border = Swing.EmptyBorder(30, 30, 30, 30)
      background = new Color(229, 255, 204)
    }
    setVisible
  }
  
  /**
   * Builds result GUI.
   */
  def buildResultGUI(dir: String, linksSuccess: Set[String], linksFailed: Set[String], 
                     ignCnt: Int, lmtCnt: Int) {
    title = "Site Downloader"
    
    val labelSum = new Label {
      text = "Summary"
      font = new Font("Arial", 24, 20)
    } 
      
    val label1 = new Label {
      text = "A list of successful URLs"
    }
    
    val textArea1 = new TextArea {
      text = (for (url <- linksSuccess) yield url).mkString("\n")
    }
    
    val textScrollPane1 = new ScrollPane(textArea1) {
      preferredSize = new Dimension(200,200)
    }
    
    val label2 = new Label {
      text = "A list of unsuccessful URLs"
    }
    
    val textArea2 = new TextArea {
      text = (for (url <- linksFailed) yield url).mkString("\n")
    }
    
    val textScrollPane2 = new ScrollPane(textArea2) {
      preferredSize = new Dimension(200,200)
    }
    
    val label3 = new Label {
      text = "Total number of inappropriate URLs ignored: " + ignCnt
    }
    
    val label4 = new Label {
      text = "Total number of appropriate URLs beyond the depth limit: " + lmtCnt
    }
    
    val labelDir = new Label {
      text = "All content downloaded to local folder: " + dir.substring(0, dir.length() - 1)
    }
    
    val quitButton = new Button {
      text = "Quit"
      reactions += {
        case ButtonClicked(_) => System.exit(0)
      }
    }
    
    contents = new BoxPanel(Orientation.Vertical) {
      contents += labelSum
      contents += label1
      contents += textScrollPane1
      contents += label2
      contents += textScrollPane2
      contents += label3
      contents += label4
      contents += labelDir
      contents += quitButton
      border = Swing.EmptyBorder(10, 30, 30, 30)
      background = new Color(229, 255, 204)
    }
    setVisible
  }
  
  /** Sets this frame visible */
  private def setVisible() { visible = true }
  
  /** Helper that shows the input dialog */
  def showInputDialog(prompt: String): String = {
    Dialog.showInput(null, prompt, initial = "") match {
      case None => 
        showInfoDialog("Error", "Please enter a value!")
        showInputDialog(prompt)
      case Some(s) if s.isEmpty => 
        showInfoDialog("Error", "Please enter a value!")
        showInputDialog(prompt)
      case Some(s) => 
        return s
    }
  }
  
  /** Helper that shows the input dialog (for number inputs) */
  def showInputNumberDialog(prompt: String): Int = {
    try showInputDialog(prompt).toInt
    catch {
      case e: NumberFormatException => 
        showInfoDialog("Error", "Please enter a valid number!")
        showInputNumberDialog(prompt)
    }
  }
  
  /** Helper that shows the information dialog */
  def showInfoDialog(title: String, content: String) {
    Dialog.showMessage(null, content, title)
  }
  
  /** Helper that shows the confirmation dialog */
  def showConfirmDialog(title: String, content: String) = {
    Dialog.showConfirmation(null, content, title)
  }
  
  /** Helper that shows the FileChooser */
  def showDirChooser(): String = {
    val chooser = new FileChooser()
    chooser.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    import FileChooser.Result._
    chooser.showOpenDialog(null) match {
      case Approve =>
        val dir = chooser.selectedFile
        if (dir.list().length > 0) {
          showConfirmDialog("Confirmation", "This directory is not empty, continue?") match {
            case Ok => dir.getPath()
            case _ => showDirChooser()
          }
        }
        else {
          dir.getPath()
        }
      case Cancel =>
        showInfoDialog("Error", "Please choose a directory!")
        showDirChooser()
      case Error =>
        showInfoDialog("Error", "Unable to open this directory!")
        showDirChooser()
    }
  }
}