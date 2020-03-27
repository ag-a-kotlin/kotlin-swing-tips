package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.JTextComponent
import javax.swing.undo.UndoManager

fun makeUI(): Component {
  val field1 = JTextField("1111111111")
  initUndoRedo(field1)
  val field2 = JTextField("2222222222")
  initUndoRedo(field2)
  return JPanel(GridLayout(2, 1)).also {
    it.add(makeTitledPanel("undo:Ctrl-z, redo:Ctrl-y", field1))
    it.add(makeTitledPanel("test", field2))
    it.border = BorderFactory.createEmptyBorder(10, 5, 10, 5)
    it.preferredSize = Dimension(320, 240)
  }
}

private class UndoAction(private val undoManager: UndoManager) : AbstractAction("undo") {
  override fun actionPerformed(e: ActionEvent) {
    runCatching {
      undoManager.undo()
    }.onFailure {
      UIManager.getLookAndFeel().provideErrorFeedback(e.source as? Component)
    }
  }
}

private class RedoAction(private val undoManager: UndoManager) : AbstractAction("redo") {
  override fun actionPerformed(e: ActionEvent) {
    runCatching {
      undoManager.redo()
    }.onFailure {
      UIManager.getLookAndFeel().provideErrorFeedback(e.source as? Component)
    }
  }
}

private fun initUndoRedo(tc: JTextComponent) {
  val manager = UndoManager()
  tc.document.addUndoableEditListener(manager)
  tc.actionMap.put("undo", UndoAction(manager))
  tc.actionMap.put("redo", RedoAction(manager))
  val modifiers = Toolkit.getDefaultToolkit().menuShortcutKeyMask
  // Java 10: val modifiers = Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx()
  val im = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
  im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifiers), "undo")
  im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifiers or InputEvent.SHIFT_MASK), "redo")
  im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifiers), "redo")
}

private fun makeTitledPanel(title: String, cmp: Component): Component {
  val p = JPanel(GridBagLayout())
  p.border = BorderFactory.createTitledBorder(title)
  val c = GridBagConstraints()
  c.weightx = 1.0
  c.fill = GridBagConstraints.HORIZONTAL
  c.insets = Insets(5, 5, 5, 5)
  p.add(cmp, c)
  return p
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
