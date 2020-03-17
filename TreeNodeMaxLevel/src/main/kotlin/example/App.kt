package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

private val countLabel = JLabel("PathCount: ")
private val levelLabel = JLabel("Level: ")

fun makeUI(): Component {
  val tree = JTree()
  tree.componentPopupMenu = TreePopupMenu()
  tree.selectionModel.addTreeSelectionListener { e ->
    e.newLeadSelectionPath?.also { updateLabel(it) }
  }

  val check = JCheckBox("JTree#setRootVisible(...)", true)
  check.addActionListener { tree.isRootVisible = (it.source as? JCheckBox)?.isSelected == true }

  val p = JPanel(GridLayout(0, 1, 2, 2))
  p.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
  p.add(countLabel)
  p.add(levelLabel)

  return JPanel(BorderLayout()).also {
    it.add(check, BorderLayout.NORTH)
    it.add(JScrollPane(tree))
    it.add(p, BorderLayout.SOUTH)
    it.preferredSize = Dimension(320, 240)
  }
}

private fun updateLabel(path: TreePath) {
  countLabel.text = "PathCount: " + path.pathCount
  val o = path.lastPathComponent
  if (o is DefaultMutableTreeNode) {
    levelLabel.text = "Level: " + o.level
  }
}

private class TreePopupMenu : JPopupMenu() {
  override fun show(c: Component, x: Int, y: Int) {
    if (c is JTree && c.selectionCount > 0) {
      super.show(c, x, y)
    }
  }

  companion object {
    private const val NODE_MAXIMUM_LEVELS = 2
  }

  init {
    add("path").addActionListener {
      val tree = invoker as? JTree ?: return@addActionListener
      tree.selectionPath?.also {
        updateLabel(it)
      }
      JOptionPane.showMessageDialog(tree, tree.selectionPaths, "path", JOptionPane.INFORMATION_MESSAGE)
    }
    add("add").addActionListener {
      val tree = invoker as? JTree ?: return@addActionListener
      val path = tree.selectionPath
      if (path != null && path.pathCount <= NODE_MAXIMUM_LEVELS) {
        val model = tree.model as DefaultTreeModel
        val self = path.lastPathComponent as DefaultMutableTreeNode
        val child = DefaultMutableTreeNode("New child node")
        self.add(child)
        model.reload(self)
      } else {
        val message = "ERROR: Maximum levels of $NODE_MAXIMUM_LEVELS exceeded."
        JOptionPane.showMessageDialog(tree, message, "add node", JOptionPane.ERROR_MESSAGE)
      }
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
