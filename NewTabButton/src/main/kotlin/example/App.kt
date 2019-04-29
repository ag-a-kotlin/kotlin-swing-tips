package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.Serializable
import java.util.Random
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout()) {
  init {
    UIManager.put("example.TabButton", "TabViewButtonUI")
    UIManager.put("TabViewButtonUI", "example.OperaTabViewButtonUI")
    val tab3 = CardLayoutTabbedPane()
    tab3.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(opera like)"))
    tab3.addTab("9999", JScrollPane(JTree()))
    tab3.addTab("aaaaaaaaaaaaaaaaaaaaaaa", JLabel("hhhhh"))
    tab3.addTab("bbbb", JLabel("iiii"))
    tab3.addTab("cccc", JButton("jjjjjj"))
    add(tab3)
    setPreferredSize(Dimension(320, 240))
  }

  companion object {

    @JvmStatic
    fun main(args: Array<String>) {
      EventQueue.invokeLater(object : Runnable {
        override fun run() {
          createAndShowGui()
        }
      })
    }

    fun createAndShowGui() {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      } catch (ex: ClassNotFoundException) {
        ex.printStackTrace()
      } catch (ex: InstantiationException) {
        ex.printStackTrace()
      } catch (ex: IllegalAccessException) {
        ex.printStackTrace()
      } catch (ex: UnsupportedLookAndFeelException) {
        ex.printStackTrace()
      }

      val frame = JFrame("@title@")
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.getContentPane().add(MainPanel())
      frame.pack()
      frame.setLocationRelativeTo(null)
      frame.setVisible(true)
    }
  }
}

internal class CardLayoutTabbedPane : JPanel(BorderLayout()) {
  protected val cardLayout = CardLayout()
  protected val tabPanel = JPanel(TabLayout())
  protected val wrapPanel = JPanel(BorderLayout())
  protected val contentsPanel = JPanel(cardLayout)
  protected val bg = ButtonGroup()
  private val button = JButton(PlusIcon())

  // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
  private val icons = listOf(
    ImageIcon(javaClass.getResource("wi0009-16.png")),
    ImageIcon(javaClass.getResource("wi0054-16.png")),
    ImageIcon(javaClass.getResource("wi0062-16.png")),
    ImageIcon(javaClass.getResource("wi0063-16.png")),
    ImageIcon(javaClass.getResource("wi0064-16.png")),
    ImageIcon(javaClass.getResource("wi0096-16.png")),
    ImageIcon(javaClass.getResource("wi0111-16.png")),
    ImageIcon(javaClass.getResource("wi0122-16.png")),
    ImageIcon(javaClass.getResource("wi0124-16.png")),
    ImageIcon(javaClass.getResource("wi0126-16.png")))

  init {
    val left = 0
    val right = 0
    tabPanel.setBorder(BorderFactory.createMatteBorder(0, left, 0, right, Color(20, 30, 50)))
    contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, left, 2, right))

    tabPanel.setOpaque(true)
    tabPanel.setBackground(Color(20, 30, 50))

    wrapPanel.setOpaque(true)
    wrapPanel.setBackground(Color(20, 30, 50))

    // contentsPanel.setOpaque(true);
    // contentsPanel.setBackground(new Color(20, 30, 50));

    wrapPanel.add(tabPanel)
    // TEST: wrapPanel.add(new JButton("a"), BorderLayout.WEST);

    // JPanel locPanel = new JPanel();
    // wrapPanel.add(new JButton("b"), BorderLayout.SOUTH);

    add(wrapPanel, BorderLayout.NORTH)
    add(contentsPanel)

    button.setBorder(BorderFactory.createEmptyBorder())
    button.addActionListener(object : ActionListener {
      private var count: Int = 0
      override fun actionPerformed(e: ActionEvent) {
        addTab("new tab:$count", JLabel("xxx:$count"))
        count++
      }
    })
  }

  protected fun createTabComponent(title: String, comp: Component): Component {
    // TabButton tab = new TabButton(new AbstractAction(title) {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     cardLayout.show(contentsPanel, title);
    //   }
    // });
    val tab = TabButton(title)
    tab.addMouseListener(object : MouseAdapter() {
      override fun mousePressed(e: MouseEvent) {
        (e.getComponent() as? AbstractButton)?.setSelected(true)
        cardLayout.show(contentsPanel, title)
      }
    })
    tab.setIcon(icons.get(Random().nextInt(icons.size)))
    tab.setLayout(BorderLayout())
    val close = object : JButton(CloseTabIcon(Color.GRAY)) {
      override fun getPreferredSize(): Dimension {
        return Dimension(12, 12)
      }
    }
    close.addActionListener {
      tabPanel.remove(tab)
      contentsPanel.remove(comp)
      val isMoreThanOne = tabPanel.getComponentCount() > 1
      if (isMoreThanOne) {
        tabPanel.revalidate()
        val b = tabPanel.getComponent(0) as TabButton
        b.setSelected(true)
        cardLayout.first(contentsPanel)
      }
      tabPanel.revalidate()
    }
    close.setBorder(BorderFactory.createEmptyBorder())
    close.setFocusPainted(false)
    close.setContentAreaFilled(false)
    close.setPressedIcon(CloseTabIcon(Color.BLACK))
    close.setRolloverIcon(CloseTabIcon(Color.ORANGE))

    val p = JPanel(BorderLayout())
    p.setOpaque(false)
    p.add(close, BorderLayout.NORTH)
    tab.add(p, BorderLayout.EAST)
    bg.add(tab)
    tab.setSelected(true)
    return tab
  }

  fun addTab(title: String, comp: Component) {
    tabPanel.remove(button)
    tabPanel.add(createTabComponent(title, comp))
    tabPanel.add(button)
    tabPanel.revalidate()
    contentsPanel.add(comp, title)
    cardLayout.show(contentsPanel, title)
  }
}

internal class TabLayout : LayoutManager, Serializable {
  override fun addLayoutComponent(name: String, comp: Component) { /* not needed */
  }

  override fun removeLayoutComponent(comp: Component) { /* not needed */
  }

  override fun preferredLayoutSize(parent: Container): Dimension {
    synchronized(parent.getTreeLock()) {
      val last = parent.getComponentCount() - 1
      var w = 0
      var h = 0
      if (last >= 0) {
        val comp = parent.getComponent(last)
        val d = comp.getPreferredSize()
        w = d.width
        h = d.height
      }
      val i = parent.getInsets()
      return Dimension(i.left + i.right + w, i.top + i.bottom + h)
    }
  }

  override fun minimumLayoutSize(parent: Container): Dimension {
    synchronized(parent.getTreeLock()) {
      return Dimension(100, 24)
    }
  }

  override fun layoutContainer(parent: Container) {
    synchronized(parent.getTreeLock()) {
      val ncomponents = parent.getComponentCount()
      if (ncomponents == 0) {
        return
      }
      // int nrows = 1;
      // boolean ltr = parent.getComponentOrientation().isLeftToRight()
      val insets = parent.getInsets()
      val ncols = ncomponents - 1
      val lastw = parent.getComponent(ncomponents - 1).getPreferredSize().width
      val width = parent.getWidth() - insets.left - insets.right - lastw
      val h = parent.getHeight() - insets.top - insets.bottom
      val w = if (width > TAB_WIDTH * ncols) TAB_WIDTH else width / ncols
      var gap = width - w * ncols
      var x = insets.left
      val y = insets.top
      for (i in 0 until ncomponents) {
        val cw = if (i == ncols) lastw else w + if (gap-- > 0) 1 else 0
        parent.getComponent(i).setBounds(x, y, cw, h)
        x += cw
      }
    }
  }

  override fun toString(): String {
    return javaClass.getName()
  }

  companion object {
    private const val serialVersionUID = 1L
    private val TAB_WIDTH = 100
  }
}

fun main() {
  EventQueue.invokeLater {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (ex: ClassNotFoundException) {
      ex.printStackTrace()
    } catch (ex: InstantiationException) {
      ex.printStackTrace()
    } catch (ex: IllegalAccessException) {
      ex.printStackTrace()
    } catch (ex: UnsupportedLookAndFeelException) {
      ex.printStackTrace()
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
