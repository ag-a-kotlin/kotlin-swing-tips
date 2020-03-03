package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

fun makeUI(): Component {
  val model = RowDataModel()
  model.addRowData(RowData("Name 1", "comment..."))
  model.addRowData(RowData("Name 2", "Test"))
  model.addRowData(RowData("Name d", "ee"))
  model.addRowData(RowData("Name c", "Test cc"))
  model.addRowData(RowData("Name b", "Test bb"))
  model.addRowData(RowData("Name a", "ff"))
  model.addRowData(RowData("Name 0", "Test aa"))
  val table = JTable(model)

  val renderer = StripeTableRenderer()
  table.setDefaultRenderer(String::class.java, renderer)
  table.setDefaultRenderer(Number::class.java, renderer)
  table.setShowGrid(false)

  val col = table.columnModel.getColumn(0)
  col.minWidth = 60
  col.maxWidth = 60
  col.resizable = false
  table.autoCreateRowSorter = true
  table.fillsViewportHeight = true
  table.componentPopupMenu = TablePopupMenu()
  return JPanel(BorderLayout()).also {
    it.add(JScrollPane(table))
    it.preferredSize = Dimension(320, 240)
  }
}

private class StripeTableRenderer : DefaultTableCellRenderer() {
  override fun getTableCellRendererComponent(
    table: JTable,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int
  ): Component {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    if (isSelected) {
      foreground = table.selectionForeground
      background = table.selectionBackground
    } else {
      foreground = table.foreground
      background = if (row % 2 == 0) EVEN_COLOR else table.background
    }
    horizontalAlignment = if (value is Number) SwingConstants.RIGHT else SwingConstants.LEFT
    return this
  }

  companion object {
    private val EVEN_COLOR = Color(0xF0_F0_FF)
  }
}

private class RowDataModel : DefaultTableModel() {
  private var number = 0
  fun addRowData(t: RowData) {
    val obj = arrayOf(number, t.name, t.comment)
    super.addRow(obj)
    number++
  }

  override fun isCellEditable(row: Int, col: Int) = COLUMN_ARRAY[col].isEditable

  override fun getColumnClass(column: Int) = COLUMN_ARRAY[column].columnClass

  override fun getColumnCount() = COLUMN_ARRAY.size

  override fun getColumnName(column: Int) = COLUMN_ARRAY[column].columnName

  private data class ColumnContext(
    val columnName: String,
    val columnClass: Class<*>,
    val isEditable: Boolean
  )

  companion object {
    private val COLUMN_ARRAY = arrayOf(
      ColumnContext("No.", Number::class.java, false),
      ColumnContext("Name", String::class.java, true),
      ColumnContext("Comment", String::class.java, true)
    )
  }
}

private data class RowData(val name: String, val comment: String)

private class TablePopupMenu : JPopupMenu() {
  private val delete: JMenuItem
  override fun show(c: Component, x: Int, y: Int) {
    if (c is JTable) {
      delete.isEnabled = c.selectedRowCount > 0
      super.show(c, x, y)
    }
  }

  init {
    add("add").addActionListener {
      val table = invoker as? JTable ?: return@addActionListener
      val model = table.model as? RowDataModel ?: return@addActionListener
      model.addRowData(RowData("New row", ""))
      val r = table.getCellRect(model.rowCount - 1, 0, true)
      table.scrollRectToVisible(r)
    }
    addSeparator()
    delete = add("delete")
    delete.addActionListener {
      val table = invoker as? JTable ?: return@addActionListener
      val model = table.model as? RowDataModel ?: return@addActionListener
      val selection = table.selectedRows
      for (i in selection.indices.reversed()) {
        model.removeRow(table.convertRowIndexToModel(selection[i]))
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
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      getContentPane().add(makeUI())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
