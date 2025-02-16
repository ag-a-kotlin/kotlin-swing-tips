package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ItemListener
import java.util.Calendar
import java.util.Date
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.JSpinner.DateEditor

val r1 = JRadioButton("LEADING")
val r2 = JRadioButton("CENTER")
val r3 = JRadioButton("TRAILING")

fun makeUI(): Component {
  val il = ItemListener { e ->
    val alignment = when {
      e.itemSelectable === r1 -> SwingConstants.LEADING
      e.itemSelectable === r2 -> SwingConstants.CENTER
      else -> SwingConstants.TRAILING
    }
    UIManager.put("Spinner.editorAlignment", alignment)
    SwingUtilities.updateComponentTreeUI(r1.rootPane)
  }
  val bg = ButtonGroup()
  val box = Box.createHorizontalBox()
  for (r in listOf(r1, r2, r3)) {
    r.addItemListener(il)
    bg.add(r)
    box.add(r)
  }
  val weeks = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Sat")
  val spinner0 = JSpinner(SpinnerListModel(weeks))
  val date = Date()
  val spinner1 = JSpinner(SpinnerDateModel(date, date, null, Calendar.DAY_OF_MONTH))
  spinner1.editor = DateEditor(spinner1, "yyyy/MM/dd")

  val spinner2 = JSpinner(SpinnerNumberModel(5, 0, 10, 1))

  val p = JPanel(GridBagLayout())
  val c = GridBagConstraints()
  c.gridheight = 1
  c.gridwidth = 1
  c.gridx = 0
  c.weightx = 0.0
  c.insets = Insets(5, 5, 5, 0)
  c.anchor = GridBagConstraints.EAST
  c.gridy = 0
  p.add(JLabel("SpinnerListModel: "), c)
  c.gridy = 1
  p.add(JLabel("SpinnerDateModel: "), c)
  c.gridy = 2
  p.add(JLabel("SpinnerNumberModel: "), c)
  c.gridx = 1
  c.weightx = 1.0
  c.fill = GridBagConstraints.HORIZONTAL
  c.gridy = 0
  p.add(spinner0, c)
  c.gridy = 1
  p.add(spinner1, c)
  c.gridy = 2
  p.add(spinner2, c)

  return JPanel(BorderLayout()).also {
    it.add(box, BorderLayout.NORTH)
    it.add(p)
    EventQueue.invokeLater {
      val mb = JMenuBar()
      mb.add(LookAndFeelUtil.createLookAndFeelMenu())
      it.rootPane.jMenuBar = mb
    }
    it.border = BorderFactory.createEmptyBorder(10, 5, 10, 5)
    it.preferredSize = Dimension(320, 240)
  }
}

private object LookAndFeelUtil {
  private var lookAndFeel = UIManager.getLookAndFeel().javaClass.name
  fun createLookAndFeelMenu() = JMenu("LookAndFeel").also {
    val lafRadioGroup = ButtonGroup()
    for (lafInfo in UIManager.getInstalledLookAndFeels()) {
      it.add(createLookAndFeelItem(lafInfo.name, lafInfo.className, lafRadioGroup))
    }
  }

  private fun createLookAndFeelItem(
    lafName: String,
    lafClassName: String,
    lafGroup: ButtonGroup
  ): JMenuItem {
    val lafItem = JRadioButtonMenuItem(lafName, lafClassName == lookAndFeel)
    lafItem.actionCommand = lafClassName
    lafItem.hideActionText = true
    lafItem.addActionListener { e ->
      val m = lafGroup.selection
      runCatching {
        setLookAndFeel(m.actionCommand)
      }.onFailure {
        UIManager.getLookAndFeel().provideErrorFeedback(e.source as? Component)
      }
    }
    lafGroup.add(lafItem)
    return lafItem
  }

  @Throws(
    ClassNotFoundException::class,
    InstantiationException::class,
    IllegalAccessException::class,
    UnsupportedLookAndFeelException::class
  )
  private fun setLookAndFeel(lookAndFeel: String) {
    val oldLookAndFeel = LookAndFeelUtil.lookAndFeel
    if (oldLookAndFeel != lookAndFeel) {
      UIManager.setLookAndFeel(lookAndFeel)
      LookAndFeelUtil.lookAndFeel = lookAndFeel
      updateLookAndFeel()
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel)
    }
  }

  private fun updateLookAndFeel() {
    for (window in Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window)
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
