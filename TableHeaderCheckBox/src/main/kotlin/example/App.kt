package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.plaf.ColorUIResource
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn

class MainPanel : JPanel(BorderLayout()) {
  private val columnNames = arrayOf<Any>(Status.INDETERMINATE, "Integer", "String")
  private val data = arrayOf(
    arrayOf<Any>(true, 1, "BBB"),
    arrayOf<Any>(false, 12, "AAA"),
    arrayOf<Any>(true, 2, "DDD"),
    arrayOf<Any>(false, 5, "CCC"),
    arrayOf<Any>(true, 3, "EEE"),
    arrayOf<Any>(false, 6, "GGG"),
    arrayOf<Any>(true, 4, "FFF"),
    arrayOf<Any>(false, 7, "HHH")
  )
  private val model = object : DefaultTableModel(data, columnNames) {
    override fun getColumnClass(column: Int) = getValueAt(0, column).javaClass
  }
  private val table = object : JTable(model) {
    @Transient
    protected var handler: HeaderCheckBoxHandler? = null

    override fun updateUI() {
      // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
      // https://bugs.openjdk.java.net/browse/JDK-6788475
      // XXX: set dummy ColorUIResource
      setSelectionForeground(ColorUIResource(Color.RED))
      setSelectionBackground(ColorUIResource(Color.RED))
      getTableHeader()?.removeMouseListener(handler)
      getModel()?.removeTableModelListener(handler)
      super.updateUI()

      getModel()?.also {
        for (i in 0 until it.getColumnCount()) {
          val r = getDefaultRenderer(it.getColumnClass(i)) as? Component ?: continue
          SwingUtilities.updateComponentTreeUI(r)
        }
        handler = HeaderCheckBoxHandler(this, CHECKBOX_COLUMN)
        it.addTableModelListener(handler)
        getTableHeader().addMouseListener(handler)
      }
      getColumnModel().getColumn(CHECKBOX_COLUMN).also {
        it.setHeaderRenderer(HeaderRenderer())
        it.setHeaderValue(Status.INDETERMINATE)
      }
    }

    override fun prepareEditor(editor: TableCellEditor, row: Int, column: Int): Component {
      val c = super.prepareEditor(editor, row, column)
      (c as? JCheckBox)?.also {
        it.setBackground(getSelectionBackground())
        it.setBorderPainted(true)
      }
      return c
    }
  }

  init {
    table.setFillsViewportHeight(true)
    add(JScrollPane(table))
    setPreferredSize(Dimension(320, 240))
  }

  companion object {
    private const val CHECKBOX_COLUMN = 0
  }
}

internal class HeaderRenderer : TableCellRenderer {
  private val check = JCheckBox("")
  private val label = JLabel("Check All")

  override fun getTableCellRendererComponent(
    table: JTable,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int
  ): Component {
    val status = value as? Status ?: Status.INDETERMINATE
    when (status) {
      Status.SELECTED -> updateCheckBox(true, true)
      Status.DESELECTED -> updateCheckBox(false, true)
      Status.INDETERMINATE -> updateCheckBox(true, false)
      // else -> throw AssertionError("Unknown Status")
    }
    check.setOpaque(false)
    check.setFont(table.getFont())
    val r = table.getTableHeader().getDefaultRenderer()
    val l = r.getTableCellRendererComponent(table, status, isSelected, hasFocus, row, column) as JLabel
    label.setIcon(ComponentIcon(check))
    l.setIcon(ComponentIcon(label))
    l.setText(null) // XXX: Nimbus???
    return l
  }

  private fun updateCheckBox(isSelected: Boolean, isEnabled: Boolean) {
    check.setSelected(isSelected)
    check.setEnabled(isEnabled)
  }
}

class HeaderCheckBoxHandler(val table: JTable, val targetColumnIndex: Int) : MouseAdapter(), TableModelListener {

  override fun tableChanged(e: TableModelEvent) {
    if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
      val vci = table.convertColumnIndexToView(targetColumnIndex)
      val column = table.getColumnModel().getColumn(vci)
      val status = column.getHeaderValue()
      val m = table.getModel()
      if (m is DefaultTableModel && fireUpdateEvent(m, column, status)) {
        val h = table.getTableHeader()
        h.repaint(h.getHeaderRect(vci))
      }
    }
  }

  private fun fireUpdateEvent(m: DefaultTableModel, column: TableColumn, status: Any): Boolean {
    return if (Status.INDETERMINATE == status) {
      val l = m.getDataVector().mapNotNull { (it as? List<*>)?.get(targetColumnIndex) as? Boolean }.distinct()
      val isOnlyOneSelected = l.size == 1
      if (isOnlyOneSelected) {
        // column.setHeaderValue(if (l.get(0)) Status.SELECTED else Status.DESELECTED)
        column.setHeaderValue(if (l.first()) Status.SELECTED else Status.DESELECTED)
        true
      } else {
        false
      }
    } else {
      column.setHeaderValue(Status.INDETERMINATE)
      true
    }
  }

  override fun mouseClicked(e: MouseEvent) {
    val tbl = (e.getComponent() as? JTableHeader)?.getTable() ?: return
    val columnModel = tbl.getColumnModel()
    val m = tbl.getModel()
    val vci = columnModel.getColumnIndexAtX(e.getX())
    val mci = tbl.convertColumnIndexToModel(vci)
    if (mci == targetColumnIndex && m.getRowCount() > 0) {
      val column = columnModel.getColumn(vci)
      val b = Status.DESELECTED === column.getHeaderValue()
      for (i in 0 until m.getRowCount()) {
        m.setValueAt(b, i, mci)
      }
      column.setHeaderValue(if (b) Status.SELECTED else Status.DESELECTED)
    }
  }
}

internal class ComponentIcon(private val cmp: Component) : Icon {

  override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
    SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight())
  }

  override fun getIconWidth() = cmp.getPreferredSize().width

  override fun getIconHeight() = cmp.getPreferredSize().height
}

internal enum class Status {
  SELECTED, DESELECTED, INDETERMINATE
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
