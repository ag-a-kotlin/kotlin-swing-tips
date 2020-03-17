package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.DragSource
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.IOException
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.metal.MetalTabbedPaneUI

class MainPanel : JPanel(BorderLayout()) {
  init {
    val sub = DnDTabbedPane().also {
      it.addTab("Title aa", JLabel("aaa"))
      it.addTab("Title bb", JScrollPane(JTree()))
      it.addTab("Title cc", JScrollPane(JTextArea("JTextArea cc")))
    }

    val tabbedPane = DnDTabbedPane().also {
      it.addTab("JTree 00", JScrollPane(JTree()))
      it.addTab("JLabel 01", JLabel("Test"))
      it.addTab("JTable 02", JScrollPane(JTable(10, 3)))
      it.addTab("JTextArea 03", JScrollPane(JTextArea("JTextArea 03")))
      it.addTab("JLabel 04", JLabel("<html>1111111111111111<br>13412341234123446745"))
      it.addTab("null 05", null)
      it.addTab("JTabbedPane 06", sub)
      it.addTab("Title 000000000000000007", JScrollPane(JTree()))
    }

    val sub2 = DnDTabbedPane().also {
      it.addTab("Title aaa", JLabel("aaa"))
      it.addTab("Title bbb", JScrollPane(JTree()))
      it.addTab("Title ccc", JScrollPane(JTextArea("JTextArea ccc")))
    }

    tabbedPane.setName("JTabbedPane#main")
    sub.setName("JTabbedPane#sub1")
    sub2.setName("JTabbedPane#sub2")

    val dropTargetListener = TabDropTargetAdapter()
    val handler = TabTransferHandler()
    listOf(tabbedPane, sub, sub2).forEach { tp ->
      tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT)
      tp.setTransferHandler(handler)
      runCatching {
        tp.getDropTarget().addDropTargetListener(dropTargetListener)
      }.onFailure { ex -> // catch (ex: TooManyListenersException) {
        ex.printStackTrace()
        Toolkit.getDefaultToolkit().beep()
      }
    }

    val p = JPanel(GridLayout(2, 1))
    p.add(tabbedPane)
    p.add(sub2)
    add(p)
    add(makeCheckBoxPanel(tabbedPane), BorderLayout.NORTH)
    setPreferredSize(Dimension(320, 240))
  }

  private fun makeCheckBoxPanel(tabs: JTabbedPane): Component {
    val tc = JCheckBox("Top", true)
    tc.addActionListener {
      tabs.setTabPlacement(if (tc.isSelected()) SwingConstants.TOP else SwingConstants.RIGHT)
    }
    val sc = JCheckBox("SCROLL_TAB_LAYOUT", true)
    sc.addActionListener {
      tabs.setTabLayoutPolicy(if (sc.isSelected()) JTabbedPane.SCROLL_TAB_LAYOUT else JTabbedPane.WRAP_TAB_LAYOUT)
    }
    return JPanel(FlowLayout(FlowLayout.LEFT)).also {
      it.add(tc)
      it.add(sc)
    }
  }
}

class DnDTabbedPane : JTabbedPane() {
  // private val dropMode = DropMode.INSERT
  var dragTabIndex = -1
  @Transient
  var dropLocation: DropLocation? = null

  fun getDropLineRect(): Rectangle {
    // val index = dropLocation?.takeIf { it.canDrop }?.index ?: -1
    val index = dropLocation?.index ?: -1
    if (index < 0) {
      RECT_LINE.setBounds(0, 0, 0, 0)
      return RECT_LINE
    }
    val a = minOf(index, 1)
    val r = getBoundsAt(a * (index - 1))
    if (isTopBottomTabPlacement(getTabPlacement())) {
      RECT_LINE.setBounds(r.x - LINE_SIZE / 2 + r.width * a, r.y, LINE_SIZE, r.height)
    } else {
      RECT_LINE.setBounds(r.x, r.y - LINE_SIZE / 2 + r.height * a, r.width, LINE_SIZE)
    }
    return RECT_LINE
  }

  val tabAreaBounds: Rectangle
    get() {
      val tabbedRect = getBounds()
      val xx = tabbedRect.x
      val yy = tabbedRect.y
      val compRect = getSelectedComponent()?.getBounds() ?: Rectangle()
      val tabPlacement = getTabPlacement()
      if (isTopBottomTabPlacement(tabPlacement)) {
        tabbedRect.height = tabbedRect.height - compRect.height
        if (tabPlacement == SwingConstants.BOTTOM) {
          tabbedRect.y += compRect.y + compRect.height
        }
      } else {
        tabbedRect.width = tabbedRect.width - compRect.width
        if (tabPlacement == SwingConstants.RIGHT) {
          tabbedRect.x += compRect.x + compRect.width
        }
      }
      tabbedRect.translate(-xx, -yy)
      return tabbedRect
    }

  class DropLocation(pt: Point, val index: Int) : TransferHandler.DropLocation(pt)
  // {
  //   var canDrop = true
  // }

  private fun clickArrowButton(actionKey: String) {
    var scrollForwardButton: JButton? = null
    var scrollBackwardButton: JButton? = null
    for (c in getComponents()) {
      val b = c as? JButton ?: continue
      if (scrollForwardButton == null) {
        scrollForwardButton = b
      } else if (scrollBackwardButton == null) {
        scrollBackwardButton = b
      }
    }
    val button = if ("scrollTabsForwardAction" == actionKey) scrollForwardButton else scrollBackwardButton
    button?.takeIf { it.isEnabled() }?.doClick()
  }

  fun autoScrollTest(pt: Point) {
    val r = tabAreaBounds
    // int tabPlacement = getTabPlacement()
    // if (tabPlacement == TOP || tabPlacement == BOTTOM) {
    if (isTopBottomTabPlacement(getTabPlacement())) {
      RECT_BACKWARD.setBounds(r.x, r.y, SCROLL_SIZE, r.height)
      RECT_FORWARD.setBounds(r.x + r.width - SCROLL_SIZE - BUTTON_SIZE, r.y, SCROLL_SIZE + BUTTON_SIZE, r.height)
    } else { // if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      RECT_BACKWARD.setBounds(r.x, r.y, r.width, SCROLL_SIZE)
      RECT_FORWARD.setBounds(r.x, r.y + r.height - SCROLL_SIZE - BUTTON_SIZE, r.width, SCROLL_SIZE + BUTTON_SIZE)
    }
    if (RECT_BACKWARD.contains(pt)) {
      clickArrowButton("scrollTabsBackwardAction")
    } else if (RECT_FORWARD.contains(pt)) {
      clickArrowButton("scrollTabsForwardAction")
    }
  }

  init {
    val h = Handler()
    addMouseListener(h)
    addMouseMotionListener(h)
    addPropertyChangeListener(h)
  }

  // @Override TransferHandler.DropLocation dropLocationForPoint(Point p) {
  fun tabDropLocationForPoint(p: Point): DropLocation {
    // check(dropMode == DropMode.INSERT) { "Unexpected drop mode" }
    for (i in 0 until getTabCount()) {
      if (getBoundsAt(i).contains(p)) {
        return DropLocation(p, i)
      }
    }
    return if (tabAreaBounds.contains(p)) {
      DropLocation(p, getTabCount())
    } else DropLocation(p, -1)
  }

  fun updateTabDropLocation(location: DropLocation?, forDrop: Boolean): Any? {
    val old = dropLocation
    dropLocation = if (location == null || !forDrop) DropLocation(Point(), -1) else location
    firePropertyChange("dropLocation", old, dropLocation)
    return null
  }

  fun exportTab(dragIndex: Int, target: JTabbedPane, targetIndex: Int) {
    println("exportTab")
    val cmp = getComponentAt(dragIndex)
    val title = getTitleAt(dragIndex)
    val icon = getIconAt(dragIndex)
    val tip = getToolTipTextAt(dragIndex)
    val isEnabled = isEnabledAt(dragIndex)
    val tab = getTabComponentAt(dragIndex)
    // // ButtonTabComponent
    // if (tab instanceof ButtonTabComponent) {
    //   tab = new ButtonTabComponent(target)
    // }

    remove(dragIndex)
    target.insertTab(title, icon, cmp, tip, targetIndex)
    target.setEnabledAt(targetIndex, isEnabled)
    target.setTabComponentAt(targetIndex, tab)
    target.setSelectedIndex(targetIndex)
    (tab as? JComponent)?.also {
      it.scrollRectToVisible(it.getBounds())
    }
  }

  fun convertTab(prev: Int, next: Int) {
    println("convertTab")
    // if (next < 0 || prev == next) {
    //   return
    // }
    val cmp = getComponentAt(prev)
    val tab = getTabComponentAt(prev)
    val title = getTitleAt(prev)
    val icon = getIconAt(prev)
    val tip = getToolTipTextAt(prev)
    val isEnabled = isEnabledAt(prev)
    val tgtIndex = if (prev > next) next else next - 1
    remove(prev)
    insertTab(title, icon, cmp, tip, tgtIndex)
    setEnabledAt(tgtIndex, isEnabled)
    // When you drag'n'drop a disabled tab, it finishes enabled and selected.
    // pointed out by dlorde
    if (isEnabled) {
      setSelectedIndex(tgtIndex)
    }
    // I have a component in all tabs (JLabel with an X to close the tab) and when I move a tab the component disappear.
    // pointed out by Daniel Dario Morales Salas
    setTabComponentAt(tgtIndex, tab)
  }

  private inner class Handler : MouseAdapter(), PropertyChangeListener { // , BeforeDrag
    private var startPt: Point? = null
    private val gestureMotionThreshold = DragSource.getDragThreshold()

    private fun repaintDropLocation() {
      (getRootPane().getGlassPane() as? GhostGlassPane)?.also {
        it.setTargetTabbedPane(this@DnDTabbedPane)
        it.repaint()
      }
    }

    // PropertyChangeListener
    override fun propertyChange(e: PropertyChangeEvent) {
      val propertyName = e.getPropertyName()
      if ("dropLocation" == propertyName) {
        // System.out.println("propertyChange: dropLocation")
        repaintDropLocation()
      }
    }

    // MouseListener
    override fun mousePressed(e: MouseEvent) {
      val src = e.getComponent() as? DnDTabbedPane ?: return
      val isOnlyOneTab = src.getTabCount() <= 1
      if (isOnlyOneTab) {
        startPt = null
        return
      }
      val tabPt = e.getPoint() // e.getDragOrigin()
      val idx = src.indexAtLocation(tabPt.x, tabPt.y)
      val flag = idx < 0 || !src.isEnabledAt(idx) || src.getComponentAt(idx) == null
      startPt = if (flag) null else tabPt
    }

    override fun mouseDragged(e: MouseEvent) {
      val tabPt = e.getPoint() // e.getDragOrigin()
      val src = e.getComponent()
      if (tabPt.distance(startPt) > gestureMotionThreshold && src is DnDTabbedPane) {
        val th = src.getTransferHandler()
        val idx = src.indexAtLocation(tabPt.x, tabPt.y)
        val selIdx = src.selectedIndex
        val isTabRunsRotated = src.getUI() !is MetalTabbedPaneUI &&
            src.tabLayoutPolicy == WRAP_TAB_LAYOUT && idx != selIdx
        dragTabIndex = if (isTabRunsRotated) selIdx else idx
        th.exportAsDrag(src, e, TransferHandler.MOVE)
        RECT_LINE.setBounds(0, 0, 0, 0)
        src.getRootPane().getGlassPane().setVisible(true)
        src.updateTabDropLocation(DropLocation(tabPt, -1), true)
        startPt = null
      }
    }
  }

  private fun isTopBottomTabPlacement(tp: Int) = tp == TOP || tp == BOTTOM

  companion object {
    private const val SCROLL_SIZE = 20 // Test
    private const val BUTTON_SIZE = 30 // XXX 30 is magic number of scroll button size
    private const val LINE_SIZE = 3
    private val RECT_BACKWARD = Rectangle()
    private val RECT_FORWARD = Rectangle()
    private val RECT_LINE = Rectangle()
  }
}

class TabDropTargetAdapter : DropTargetAdapter() {
  private fun clearDropLocationPaint(c: Component) {
    val t = c as? DnDTabbedPane ?: return
    t.updateTabDropLocation(null, false)
    t.setCursor(Cursor.getDefaultCursor())
  }

  override fun drop(dtde: DropTargetDropEvent) {
    val c = dtde.getDropTargetContext().getComponent()
    println("DropTargetListener#drop: ${c.getName()}")
    clearDropLocationPaint(c)
  }

  override fun dragExit(dte: DropTargetEvent) {
    val c = dte.getDropTargetContext().getComponent()
    println("DropTargetListener#dragExit: ${c.getName()}")
    clearDropLocationPaint(c)
  }

  override fun dragEnter(dtde: DropTargetDragEvent) {
    val c = dtde.getDropTargetContext().getComponent()
    println("DropTargetListener#dragEnter: ${c.getName()}")
  }

  // @Override public void dragOver(DropTargetDragEvent dtde) {
  //   // System.out.println("dragOver")
  // }

  // @Override public void dropActionChanged(DropTargetDragEvent dtde) {
  //   System.out.println("dropActionChanged")
  // }
}

data class DnDTabData(val tabbedPane: DnDTabbedPane)

class TabTransferHandler : TransferHandler() {
  private val localObjectFlavor = DataFlavor(DnDTabData::class.java, "DnDTabData")
  private var source: DnDTabbedPane? = null

  // init {
  //   println("TabTransferHandler")
  // }

  override fun createTransferable(c: JComponent): Transferable? {
    println("createTransferable")
    val src: DnDTabbedPane? = c as? DnDTabbedPane
    source = src
    return object : Transferable {
      override fun getTransferDataFlavors() = arrayOf(localObjectFlavor)

      override fun isDataFlavorSupported(flavor: DataFlavor) = localObjectFlavor == flavor

      @Throws(UnsupportedFlavorException::class, IOException::class)
      override fun getTransferData(flavor: DataFlavor): Any {
        return if (isDataFlavorSupported(flavor) && src != null) {
          DnDTabData(src)
        } else {
          throw UnsupportedFlavorException(flavor)
        }
      }
    }
  }

  override fun canImport(support: TransferSupport): Boolean {
    // System.out.println("canImport")
    val target = support.getComponent()
    if (!support.isDrop() || !support.isDataFlavorSupported(localObjectFlavor) || target !is DnDTabbedPane) {
      println("canImport: ${support.isDrop()} ${support.isDataFlavorSupported(localObjectFlavor)}")
      return false
    }
    support.setDropAction(MOVE)
    val tdl = support.getDropLocation()
    val pt = tdl.getDropPoint()
    // val target = support.getComponent() as DnDTabbedPane
    target.autoScrollTest(pt)
    val dl = target.tabDropLocationForPoint(pt)
    val idx = dl.index

    val isAreaContains = target.tabAreaBounds.contains(pt) && idx >= 0
    val canDrop = if (target == source) {
      isAreaContains && idx != target.dragTabIndex && idx != target.dragTabIndex + 1
    } else {
      source?.let { !it.isAncestorOf(target) } ?: false && isAreaContains
    }

    // [JDK-6700748] Cursor flickering during D&D when using CellRendererPane with validation - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-6700748
    val cursor = if (canDrop) DragSource.DefaultMoveDrop else DragSource.DefaultMoveNoDrop
    val glassPane = target.getRootPane().getGlassPane()
    glassPane.setCursor(cursor)
    target.setCursor(cursor)

    support.setShowDropLocation(canDrop)
    // dl.canDrop = canDrop
    target.updateTabDropLocation(dl, canDrop)
    return canDrop
  }

  private fun makeDragTabImage(tabbedPane: DnDTabbedPane): BufferedImage {
    val rect = tabbedPane.getBoundsAt(tabbedPane.dragTabIndex)
    val image = BufferedImage(tabbedPane.getWidth(), tabbedPane.getHeight(), BufferedImage.TYPE_INT_ARGB)
    val g2 = image.createGraphics()
    tabbedPane.paint(g2)
    g2.dispose()
    if (rect.x < 0) {
      rect.translate(-rect.x, 0)
    }
    if (rect.y < 0) {
      rect.translate(0, -rect.y)
    }
    if (rect.x + rect.width > image.getWidth()) {
      rect.width = image.getWidth() - rect.x
    }
    if (rect.y + rect.height > image.getHeight()) {
      rect.height = image.getHeight() - rect.y
    }
    return image.getSubimage(rect.x, rect.y, rect.width, rect.height)
  }

  override fun getSourceActions(c: JComponent): Int {
    println("getSourceActions")
    val src = c as? DnDTabbedPane ?: return NONE
    src.getRootPane().setGlassPane(GhostGlassPane(src))
    return if (src.dragTabIndex < 0) {
      NONE
    } else {
      setDragImage(makeDragTabImage(src))
      src.getRootPane().getGlassPane().setVisible(true)
      MOVE
    }
  }

  override fun importData(support: TransferSupport): Boolean {
    println("importData")
    val target = support.getComponent()
    val data = runCatching {
      support.getTransferable().getTransferData(localObjectFlavor) as? DnDTabData
    }.getOrNull()
    if (target !is DnDTabbedPane || data == null) {
      return false
    }
    val src = data.tabbedPane
    val index = target.dropLocation?.index ?: -1
    if (target == src) {
      src.convertTab(src.dragTabIndex, index) // getTargetTabIndex(e.getLocation()))
    } else {
      src.exportTab(src.dragTabIndex, target, index)
    }
    return true
  }

  override fun exportDone(c: JComponent?, data: Transferable?, action: Int) {
    println("exportDone")
    val src = c as? DnDTabbedPane ?: return
    src.getRootPane().getGlassPane().setVisible(false)
    src.updateTabDropLocation(null, false)
    src.repaint()
    src.setCursor(Cursor.getDefaultCursor())
  }
}

class GhostGlassPane(private var tabbedPane: DnDTabbedPane) : JComponent() {
  init {
    setOpaque(false)
  }

  fun setTargetTabbedPane(tab: DnDTabbedPane) {
    tabbedPane = tab
  }

  override fun paintComponent(g: Graphics) {
    tabbedPane.getDropLineRect().also { rect ->
      val g2 = g.create() as Graphics2D
      val r = SwingUtilities.convertRectangle(tabbedPane, rect, this)
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f))
      g2.setPaint(Color.RED)
      g2.fill(r)
      g2.dispose()
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
      contentPane.add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
