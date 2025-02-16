package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.time.LocalDateTime
import java.time.ZoneId
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit

private const val HTML_TEXT = "<html><body>head<table id='log' border='1'></table>tail</body>"
private const val ROW_TEXT = "<tr bgColor='%s'><td>%s</td><td>%s</td></tr>"

fun makeUI(): Component {
  val htmlEditorKit = HTMLEditorKit()
  val editor = JEditorPane()
  editor.editorKit = htmlEditorKit
  editor.text = HTML_TEXT
  editor.isEditable = false

  val insertAfterStart = JButton("insertAfterStart")
  insertAfterStart.addActionListener {
    (editor.document as? HTMLDocument)?.also {
      val element = it.getElement("log")
      val date = LocalDateTime.now(ZoneId.systemDefault())
      val tag = ROW_TEXT.format("#AEEEEE", "insertAfterStart", date)
      runCatching {
        it.insertAfterStart(element, tag)
      }.onFailure {
        UIManager.getLookAndFeel().provideErrorFeedback(editor)
      }
    }
  }

  val insertBeforeEnd = JButton("insertBeforeEnd")
  insertBeforeEnd.addActionListener {
    (editor.document as? HTMLDocument)?.also {
      val element = it.getElement("log")
      val date = LocalDateTime.now(ZoneId.systemDefault())
      val tag = ROW_TEXT.format("#FFFFFF", "insertBeforeEnd", date)
      runCatching {
        it.insertBeforeEnd(element, tag)
      }.onFailure {
        UIManager.getLookAndFeel().provideErrorFeedback(editor)
      }
    }
  }

  val box = Box.createHorizontalBox()
  box.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
  box.add(Box.createHorizontalGlue())
  box.add(insertAfterStart)
  box.add(Box.createHorizontalStrut(5))
  box.add(insertBeforeEnd)

  return JPanel(BorderLayout()).also {
    it.add(JScrollPane(editor))
    it.add(box, BorderLayout.SOUTH)
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
