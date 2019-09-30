package example

import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.DragSource
import java.awt.event.ActionEvent
import java.io.IOException
// import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.tree.*

class MainPanel : JPanel(BorderLayout()) {
  init {
    val tabbedPane = JTabbedPane()
    tabbedPane.add("JList", makeListPanel())
    tabbedPane.add("JTable", makeTablePanel())
    tabbedPane.add("JTree", makeTreePanel())
    // Default drop line color: UIManager.put("List.dropLineColor", null)
    // Hide drop lines: UIManager.put("List.dropLineColor", new Color(0x0, true))
    add(tabbedPane)
    setPreferredSize(Dimension(320, 240))
  }

  private fun makeColorChooserButton(key: String): JButton {
    val button = JButton(key)
    button.addActionListener {
      val c = JColorChooser.showDialog(button.getRootPane(), key, UIManager.getColor(key))
      UIManager.put(key, c)
    }
    return button
  }

  private fun makeListPanel(): Component {
    val model = DefaultListModel<String>()
    model.addElement("1111")
    model.addElement("22222222")
    model.addElement("333333333333")
    model.addElement("****")
    val list = JList(model)
    list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
    list.setTransferHandler(ListItemTransferHandler())
    list.setDropMode(DropMode.INSERT)
    list.setDragEnabled(true)

    // Disable row Cut, Copy, Paste
    val map = list.getActionMap()
    val dummy = object : AbstractAction() {
      override fun actionPerformed(e: ActionEvent?) {
        /* Dummy action */
      }
    }
    map.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy)
    map.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy)
    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy)
    val box: Box = Box.createHorizontalBox()
    box.add(Box.createHorizontalGlue())
    box.add(makeColorChooserButton("List.dropLineColor"))
    val p = JPanel(BorderLayout())
    p.add(JScrollPane(list))
    p.add(box, BorderLayout.SOUTH)
    return p
  }

  private fun makeTablePanel(): Component {
    val columnNames = arrayOf("String", "Integer", "Boolean")
    val data = arrayOf(
      arrayOf("AAA", 12, true),
      arrayOf("aaa", 1, false),
      arrayOf("BBB", 13, true),
      arrayOf("bbb", 2, false),
      arrayOf("CCC", 15, true),
      arrayOf("ccc", 3, false),
      arrayOf("DDD", 17, true),
      arrayOf("ddd", 4, false),
      arrayOf("EEE", 18, true),
      arrayOf("eee", 5, false),
      arrayOf("FFF", 19, true),
      arrayOf("fff", 6, false),
      arrayOf("GGG", 92, true),
      arrayOf("ggg", 0, false)
    )
    val model = object : DefaultTableModel(data, columnNames) {
      override fun getColumnClass(column: Int): Class<*> {
        return when (column) {
          0 -> String::class.java
          1 -> Number::class.java
          2 -> java.lang.Boolean::class.java
          else -> super.getColumnClass(column)
        }
      }
    }
    val table = JTable(model)
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
    table.setTransferHandler(TableRowTransferHandler())
    table.setDropMode(DropMode.INSERT_ROWS)
    table.setDragEnabled(true)
    table.setFillsViewportHeight(true)

    // Disable row Cut, Copy, Paste
    val map = table.getActionMap()
    val dummy = object : AbstractAction() {
      override fun actionPerformed(e: ActionEvent) {
        /* Dummy action */
      }
    }
    map.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy)
    map.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy)
    map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy)
    val box: Box = Box.createHorizontalBox()
    box.add(Box.createHorizontalGlue())
    box.add(makeColorChooserButton("Table.dropLineColor"))
    box.add(makeColorChooserButton("Table.dropLineShortColor"))
    val p = JPanel(BorderLayout())
    p.add(JScrollPane(table))
    p.add(box, BorderLayout.SOUTH)
    return p
  }

  private fun makeTree(handler: TransferHandler): JTree {
    val tree = JTree()
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    tree.setRootVisible(false)
    tree.setDragEnabled(true)
    tree.setTransferHandler(handler)
    tree.setDropMode(DropMode.INSERT)
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)

    // Disable node Cut action
    tree.getActionMap().put(
      TransferHandler.getCutAction().getValue(Action.NAME),
      object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
          /* Dummy action */
        }
      })

    for (i in 0 until tree.rowCount) {
      tree.expandRow(i)
    }
    return tree
  }

  private fun makeTreePanel(): Component {
    val handler = TreeTransferHandler()
    val p = JPanel(GridLayout(1, 2))
    p.add(JScrollPane(makeTree(handler)))
    p.add(JScrollPane(makeTree(handler)))
    val box: Box = Box.createHorizontalBox()
    box.add(Box.createHorizontalGlue())
    box.add(makeColorChooserButton("Tree.dropLineColor"))
    val panel = JPanel(BorderLayout())
    panel.add(p)
    panel.add(box, BorderLayout.SOUTH)
    return panel
  }
}

class ListItemTransferHandler : TransferHandler() {
  private val localObjectFlavor = DataFlavor(List::class.java, "List of items")
  private var source: JList<*>? = null
  private val selectedIndices = mutableListOf<Int>()
  private var addIndex = -1 // Location where items were added
  private var addCount = 0 // Number of items added.

  override fun createTransferable(c: JComponent): Transferable? {
    val src = c as? JList<*> ?: return null
    source = src
    src.getSelectedIndices().forEach { selectedIndices.add(it) }
    val transferObjects = src.getSelectedValuesList()
    return object : Transferable {
      override fun getTransferDataFlavors() = arrayOf(localObjectFlavor)

      override fun isDataFlavorSupported(flavor: DataFlavor) = localObjectFlavor == flavor

      @Throws(UnsupportedFlavorException::class, IOException::class)
      override fun getTransferData(flavor: DataFlavor): Any {
        return if (isDataFlavorSupported(flavor)) {
          transferObjects
        } else {
          throw UnsupportedFlavorException(flavor)
        }
      }
    }
  }

  override fun canImport(info: TransferSupport) =
      info.isDrop() &&
      info.isDataFlavorSupported(localObjectFlavor) &&
      info.getDropLocation() is JList.DropLocation

  override fun getSourceActions(c: JComponent) = MOVE // COPY_OR_MOVE

  override fun importData(info: TransferSupport): Boolean {
    val dl = info.getDropLocation()
    val target = info.getComponent()
    if (!canImport(info) || dl !is JList.DropLocation || target !is JList<*>) {
      return false
    }
    @Suppress("UNCHECKED_CAST")
    val listModel = target.getModel() as DefaultListModel<Any>
    val max = listModel.getSize()
    // var index = minOf(maxOf(0, dl.getIndex()), max)
    var index = dl.getIndex().takeIf { it in 0 until max } ?: max
    addIndex = index
    val values = runCatching {
      info.getTransferable().getTransferData(localObjectFlavor) as? List<*>
    }.getOrNull().orEmpty()
    for (o in values) {
      val i = index++
      listModel.add(i, o)
      target.addSelectionInterval(i, i)
    }
    addCount = if (target == source) values.size else 0
    return values.isNotEmpty()
  }

  override fun exportDone(c: JComponent, data: Transferable, action: Int) {
    cleanup(c, action == MOVE)
  }

  private fun cleanup(c: JComponent, remove: Boolean) {
    if (remove && selectedIndices.isNotEmpty()) {
      // If we are moving items around in the same list, we
      // need to adjust the indices accordingly, since those
      // after the insertion point have moved.
      val selectedList = when {
        addCount > 0 -> selectedIndices.map { if (it >= addIndex) it + addCount else it }
        else -> selectedIndices.toList()
      }
      ((c as? JList<*>)?.getModel() as? DefaultListModel<*>)?.also { model ->
        for (i in selectedList.indices.reversed()) {
          model.remove(selectedList[i])
        }
      }
    }
    selectedIndices.clear()
    addCount = 0
    addIndex = -1
  }
}

internal class TableRowTransferHandler : TransferHandler() {
  private var indices: IntArray? = null
  private var addIndex = -1 // Location where items were added
  private var addCount = 0 // Number of items added.

  override fun createTransferable(c: JComponent): Transferable {
    c.getRootPane().getGlassPane().setVisible(true)
    val table = c as JTable
    val model = table.model as DefaultTableModel
    indices = table.getSelectedRows()
    val transferredObjects = table.getSelectedRows().map { model.dataVector[it] }
    return object : Transferable {
      override fun getTransferDataFlavors() = arrayOf(FLAVOR)

      override fun isDataFlavorSupported(flavor: DataFlavor) = FLAVOR == flavor

      @Throws(UnsupportedFlavorException::class)
      override fun getTransferData(flavor: DataFlavor): Any {
        return if (isDataFlavorSupported(flavor)) {
          transferredObjects
        } else {
          throw UnsupportedFlavorException(flavor)
        }
      }
    }
  }

  override fun canImport(info: TransferSupport): Boolean {
    val isDroppable = info.isDrop() && info.isDataFlavorSupported(FLAVOR)
    val c = info.getComponent() as? JComponent ?: return false
    val glassPane = c.getRootPane().getGlassPane()
    glassPane.setCursor(if (isDroppable) DragSource.DefaultMoveDrop else DragSource.DefaultMoveNoDrop)
    return isDroppable
  }

  override fun getSourceActions(c: JComponent?) = MOVE

  override fun importData(info: TransferSupport): Boolean {
    if (!canImport(info)) {
      return false
    }
    val tdl = info.getDropLocation()
    if (tdl !is JTable.DropLocation) {
      return false
    }
    val target = info.getComponent() as JTable
    val model = target.getModel() as DefaultTableModel
    val max = model.getRowCount()
    var index = tdl.getRow()
    index = if (index in 0 until max) index else max
    addIndex = index
    return try {
      val values =
        info.transferable.getTransferData(FLAVOR) as List<*>
      addCount = values.size
      // val array = arrayOfNulls<Any>(0)
      for (o in values) {
        val row = index++
        val list = o as? ArrayList<*> ?: continue
        val array = arrayOfNulls<Any?>(list.size)
        list.toArray(array)
        model.insertRow(row, array)
        target.getSelectionModel().addSelectionInterval(row, row)
      }
      true
    } catch (ex: UnsupportedFlavorException) {
      false
    } catch (ex: IOException) {
      false
    }
  }

  override fun exportDone(
    c: JComponent,
    data: Transferable?,
    action: Int
  ) {
    cleanup(c, action == MOVE)
  }

  private fun cleanup(c: JComponent, remove: Boolean) {
    c.rootPane.glassPane.isVisible = false
    if (remove && indices != null) {
      val model =
        (c as JTable).model as DefaultTableModel
      if (addCount > 0) {
        for (i in indices!!.indices) {
          if (indices!![i] >= addIndex) {
            indices!![i] += addCount
          }
        }
      }
      for (i in indices!!.indices.reversed()) {
        model.removeRow(indices!![i])
      }
    }
    indices = null
    addCount = 0
    addIndex = -1
  }

  companion object {
    private val FLAVOR = DataFlavor(List::class.java, "List of items")
  }
}

internal class TreeTransferHandler : TransferHandler() {
  private var source: JTree? = null
  override fun createTransferable(c: JComponent): Transferable? {
    val src = c as? JTree ?: return null
    source = src
    val paths = src.getSelectionPaths() ?: return null
    val nodes = arrayOfNulls<DefaultMutableTreeNode?>(paths.size)
    for (i in paths.indices) {
      nodes[i] = paths[i].lastPathComponent as DefaultMutableTreeNode
    }
    return object : Transferable {
      override fun getTransferDataFlavors() = arrayOf(FLAVOR)

      override fun isDataFlavorSupported(flavor: DataFlavor) = FLAVOR == flavor

      @Throws(UnsupportedFlavorException::class)
      override fun getTransferData(flavor: DataFlavor): Any {
        return if (isDataFlavorSupported(flavor)) {
          nodes
        } else {
          throw UnsupportedFlavorException(flavor)
        }
      }
    }
  }

  override fun getSourceActions(c: JComponent) = MOVE

  override fun canImport(support: TransferSupport): Boolean {
    if (!support.isDrop()) {
      return false
    }
    if (!support.isDataFlavorSupported(FLAVOR)) {
      return false
    }
    return support.getComponent() as? JTree != source
  }

  override fun importData(support: TransferSupport): Boolean {
    val nodes = runCatching {
      support.getTransferable().getTransferData(FLAVOR) as? Array<*>
    }.getOrNull()?.filterIsInstance<DefaultMutableTreeNode>() ?: return false // .orEmpty()

    val dl = support.getDropLocation()
    if (dl is JTree.DropLocation) {
      val childIndex = dl.getChildIndex()
      val dest = dl.getPath()
      val parent = dest.getLastPathComponent() as DefaultMutableTreeNode
      val tree = support.getComponent() as JTree
      val model = tree.getModel() as DefaultTreeModel
      val idx = AtomicInteger(if (childIndex < 0) parent.childCount else childIndex)
      nodes.forEach {
        val clone = DefaultMutableTreeNode(it.getUserObject())
        model.insertNodeInto(deepCopyTreeNode(it, clone), parent, idx.incrementAndGet())
      }
      return true
    }
    return false
  }

  override fun exportDone(
    src: JComponent?,
    data: Transferable?,
    action: Int
  ) {
    if (action == MOVE && src is JTree) {
      val model = src.model as DefaultTreeModel
      val selectionPaths: Array<TreePath>? = src.selectionPaths
      if (selectionPaths != null) {
        for (path in selectionPaths) {
          model.removeNodeFromParent(path.lastPathComponent as MutableTreeNode)
        }
      }
    }
  }

  private fun deepCopyTreeNode(src: DefaultMutableTreeNode, tgt: DefaultMutableTreeNode): DefaultMutableTreeNode {
    src.children().toList()
      .filterIsInstance<DefaultMutableTreeNode>()
      .forEach {
        val clone = DefaultMutableTreeNode(it.userObject)
        tgt.add(clone)
        if (!it.isLeaf()) {
          deepCopyTreeNode(it, clone)
        }
      }
    return tgt
  }

  companion object {
    private const val NAME = "Array of DefaultMutableTreeNode"
    private val FLAVOR = DataFlavor(Array<DefaultMutableTreeNode>::class.java, NAME)
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
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      getContentPane().add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
