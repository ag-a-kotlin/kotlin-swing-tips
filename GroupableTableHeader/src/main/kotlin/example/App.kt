package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.basic.BasicTableHeaderUI
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel

class MainPanel : JPanel(BorderLayout()) {
  init {
    // http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html
    val columnNames = arrayOf("SNo.", "1", "2", "Native", "2", "3")
    val data = arrayOf(
        arrayOf<Any>("119", "foo", "bar", "ja", "ko", "zh"),
        arrayOf<Any>("911", "bar", "foo", "en", "fr", "pt"))
    val model = DefaultTableModel(data, columnNames)
    val table = object : JTable(model) {
      protected override fun createDefaultTableHeader(): JTableHeader {
        val cm = getColumnModel()
        val gname = ColumnGroup("Name")
        gname.add(cm.getColumn(1))
        gname.add(cm.getColumn(2))

        val glang = ColumnGroup("Language")
        glang.add(cm.getColumn(3))

        val gother = ColumnGroup("Others")
        gother.add(cm.getColumn(4))
        gother.add(cm.getColumn(5))

        glang.add(gother)

        val header = GroupableTableHeader(cm)
        header.addColumnGroup(gname)
        header.addColumnGroup(glang)
        return header
      }
    }
    add(JScrollPane(table))
    setPreferredSize(Dimension(320, 240))
  }
}

/**
 * GroupableTableHeader.
 * @see [GroupableTableHeader](http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html)
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 */
internal class GroupableTableHeader(model: TableColumnModel) : JTableHeader(model) {
  private val columnGroups = mutableListOf<ColumnGroup>()

  override fun updateUI() {
    super.updateUI()
    setUI(GroupableTableHeaderUI())
  }

  // [java] BooleanGetMethodName: Don't report bad method names on @Override #97
  // https://github.com/pmd/pmd/pull/97
  override fun getReorderingAllowed() = false

  override fun setReorderingAllowed(b: Boolean) {
    super.setReorderingAllowed(false)
  }

  fun addColumnGroup(g: ColumnGroup) {
    columnGroups.add(g)
  }

  fun getColumnGroups(col: TableColumn): List<*> {
    for (cg in columnGroups) {
      val groups = cg.getColumnGroupList(col, mutableListOf<Any>())
      if (!groups.isEmpty()) {
        return groups
      }
    }
    return emptyList<Any>()
  }

  @Throws(IOException::class)
  private fun writeObject(stream: ObjectOutputStream) {
    stream.defaultWriteObject()
  }

  @Throws(IOException::class, ClassNotFoundException::class)
  private fun readObject(stream: ObjectInputStream) {
    stream.defaultReadObject()
  }
}

/**
 * GroupableTableHeaderUI.
 * @see [GroupableTableHeaderUI](http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html)
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 */
internal class GroupableTableHeaderUI : BasicTableHeaderUI() {
  override fun paint(g: Graphics, c: JComponent?) {
    val clip = g.getClipBounds()
    // val left = clip.getLocation()
    // val right = Point(clip.x + clip.width - 1, clip.y)
    val cm = header.getColumnModel()
    val colMin = header.columnAtPoint(clip.getLocation())
    val colMax = header.columnAtPoint(Point(clip.x + clip.width - 1, clip.y))

    val cellRect = header.getHeaderRect(colMin)
    val headerY = cellRect.y
    val headerHeight = cellRect.height

    val map = hashMapOf<ColumnGroup, Rectangle>()
    // int columnMargin = header.getColumnModel().getColumnMargin();
    // int columnWidth;
    for (column in colMin..colMax) {
      val tc = cm.getColumn(column)
      cellRect.y = headerY
      cellRect.setSize(tc.getWidth(), headerHeight)

      var groupHeight = 0
      val cglist = (header as? GroupableTableHeader)?.getColumnGroups(tc) ?: emptyList<Any>()
      for (o in cglist) {
        val cg = o as? ColumnGroup ?: continue
        val groupRect = map.get(cg) ?: Rectangle(cellRect.getLocation(), cg.getSize(header)).also {
          map.put(cg, it)
        }
        paintCellGroup(g, groupRect, cg)
        groupHeight += groupRect.height
        cellRect.height = headerHeight - groupHeight
        cellRect.y = groupHeight
      }
      paintCell(g, cellRect, column)
      cellRect.x += cellRect.width
    }
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private fun getHeaderRenderer(columnIndex: Int): Component {
    val tc = header.getColumnModel().getColumn(columnIndex)
    val r = tc.getHeaderRenderer() ?: header.getDefaultRenderer()
    val hasFocus = !header.isPaintingForPrint() && header.hasFocus()
    // && (columnIndex == getSelectedColumnIndex())
    val table = header.getTable()
    return r.getTableCellRendererComponent(table, tc.getHeaderValue(), false, hasFocus, -1, columnIndex)
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private fun paintCell(g: Graphics, cellRect: Rectangle, columnIndex: Int) {
    val c = getHeaderRenderer(columnIndex)
    rendererPane.paintComponent(g, c, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true)
  }

  private fun paintCellGroup(g: Graphics, cellRect: Rectangle, columnGroup: ColumnGroup) {
    val r = header.getDefaultRenderer()
    val c = r.getTableCellRendererComponent(header.getTable(), columnGroup.headerValue, false, false, -1, -1)
    rendererPane.paintComponent(g, c, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true)
  }

  private fun getHeaderHeight(): Int {
    var height = 0
    val columnModel = header.getColumnModel()
    for (column in 0 until columnModel.getColumnCount()) {
      val tc = columnModel.getColumn(column)
      val comp = getHeaderRenderer(column)
      var rendererHeight = comp.getPreferredSize().height
      val cglist = (header as? GroupableTableHeader)?.getColumnGroups(tc) ?: emptyList<Any>()
      for (o in cglist) {
        val cg = o as? ColumnGroup ?: continue
        rendererHeight += cg.getSize(header).height
      }
      height = maxOf(height, rendererHeight)
    }
    return height
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  // private fun createHeaderSize(width: Long): Dimension {
  //   val w = minOf(width, Integer.MAX_VALUE.toLong())
  //   return Dimension(w.toInt(), getHeaderHeight())
  // }

  override fun getPreferredSize(c: JComponent?): Dimension {
    val width = header.getColumnModel().getColumns().toList().map { it.getPreferredWidth().toLong() }.sum()
    return Dimension(minOf(width, Integer.MAX_VALUE.toLong()).toInt(), getHeaderHeight())
    // return createHeaderSize(width)
  }
}

/**
 * ColumnGroup.
 * @see [ColumnGroup](http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html)
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 */
internal class ColumnGroup(private val text: String) {
  private val list = mutableListOf<Any>()
  val headerValue = text

  /**
   * Add TableColumn or ColumnGroup.
   * @param obj TableColumn or ColumnGroup
   */
  fun add(obj: Any?) {
    obj?.also { list.add(it) }
  }

  fun getColumnGroupList(c: TableColumn, g: MutableList<Any>): List<*> {
    g.add(this)
    // if (list.contains(c)) {
    //   return g
    // }
    // for (obj in list) {
    //   val cg = obj as? ColumnGroup ?: continue
    //   val groups = cg.getColumnGroupList(c, MutableList<Any>(g))
    //   if (!groups.isEmpty()) {
    //     return groups
    //   }
    // }
    // return emptyList<Any>()
    return when {
      list.contains(c) -> g
      else -> list.filterIsInstance(ColumnGroup::class.java)
                  .map { it.getColumnGroupList(c, ArrayList<Any>(g)) }
                  .filterNot { it.isEmpty() }.firstOrNull() ?: emptyList<Any>()
    }
  }

  fun getSize(header: JTableHeader): Dimension {
    val r = header.getDefaultRenderer()
    val c = r.getTableCellRendererComponent(header.getTable(), headerValue, false, false, -1, -1)
    var width = 0
    for (o in list) {
      // width += if (o is TableColumn) o.getWidth() else (o as ColumnGroup).getSize(header).width
      width += (o as? TableColumn)?.getWidth() ?: (o as? ColumnGroup)?.getSize(header)?.width ?: 0
    }
    return Dimension(width, c.getPreferredSize().height)
  }
}

fun main() {
  EventQueue.invokeLater {
    JFrame().apply {
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      getContentPane().add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
