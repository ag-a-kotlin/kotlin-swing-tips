package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.DefaultEditorKit
import javax.swing.text.JTextComponent

fun makeUI(): Component {
  val textArea = JTextArea("ComponentPopupMenu Test\n111111111111\n22222222\n33333333")
  textArea.componentPopupMenu = TextComponentPopupMenu()
  return JPanel(BorderLayout()).also {
    it.add(JScrollPane(textArea))
    it.preferredSize = Dimension(320, 240)
  }
}

private class TextComponentPopupMenu : JPopupMenu() {
  private val cutAction = DefaultEditorKit.CutAction()
  private val copyAction = DefaultEditorKit.CopyAction()
  private val deleteItem: JMenuItem

  init {
    add(cutAction)
    add(copyAction)
    add(DefaultEditorKit.PasteAction())
    addSeparator()
    deleteItem = add("delete")
    deleteItem.addActionListener {
      (invoker as? JTextComponent)?.replaceSelection(null)
    }
    addSeparator()
    add(DefaultEditorKit.selectAllAction).addActionListener {
      (invoker as? JTextComponent)?.selectAll()
    }
  }

  override fun show(c: Component, x: Int, y: Int) {
    if (c is JTextComponent) {
      val hasSelectedText = c.selectedText != null
      cutAction.isEnabled = hasSelectedText
      copyAction.isEnabled = hasSelectedText
      deleteItem.isEnabled = hasSelectedText
      super.show(c, x, y)
    }
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
