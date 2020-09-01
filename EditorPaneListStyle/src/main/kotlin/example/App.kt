package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.html.HTMLEditorKit

private fun makeEditorPane(): JEditorPane {
  val editorPane = JEditorPane()
  editorPane.contentType = "text/html"
  editorPane.isEditable = false
  return editorPane
}

fun makeUI(): Component {
  val html = "<html><h2>H2</h2>text<ul><li>list: %s</li></ul></html>"
  val editor0 = makeEditorPane()
  editor0.text = String.format(html, "Default")

  val cl = Thread.currentThread().contextClassLoader
  val url = cl.getResource("example/bullet.png")?.toString()
  val editor1 = makeEditorPane()
  (editor1.editorKit as? HTMLEditorKit)?.also {
    val styleSheet = it.styleSheet
    styleSheet.addRule(String.format("ul{list-style-image:url(%s);margin:0px 20px;}", url))
    // styleSheet.addRule("ul{list-style-type:circle;margin:0px 20px;}")
    // styleSheet.addRule("ul{list-style-type:disc;margin:0px 20px;}")
    // styleSheet.addRule("ul{list-style-type:square;margin:0px 20px;}")
    // styleSheet.addRule("ul{list-style-type:decimal;margin:0px 20px;}")

    // Pseudo element is not supported in javax.swing.text.html.CSS
    // styleSheet.addRule("ul{list-style-type:none;margin:0px 20px;}")
    // styleSheet.addRule("ul li:before{content: "\u00BB";}")
    editor1.text = String.format(html, "bullet.png")
  }

  return JPanel(GridLayout(2, 1)).also {
    it.add(JScrollPane(editor0))
    it.add(JScrollPane(editor1))
    it.preferredSize = Dimension(320, 240)
  }
}

fun main() {
  EventQueue.invokeLater {
    runCatching {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }.onFailure {
      it.printStackTrace()
      Toolkit.getDefaultToolkit().beep()
    }
    JFrame().apply {
      defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
      contentPane.add(makeUI())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
