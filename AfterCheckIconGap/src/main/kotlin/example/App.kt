// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout()) {
  init {
    add(JScrollPane(JTextArea()))
    setPreferredSize(Dimension(320, 240))
  }

  companion object {
    fun createMenuBar(): JMenuBar {
      println("checkIconOffset: " + UIManager.get("CheckBoxMenuItem.checkIconOffset"))
      println("afterCheckIconGap: " + UIManager.get("CheckBoxMenuItem.afterCheckIconGap"))
      println("minimumTextOffset: " + UIManager.get("CheckBoxMenuItem.minimumTextOffset"))
      println("evenHeight: " + UIManager.get("CheckBoxMenuItem.evenHeight"))

      // UIManager.put("MenuItem.checkIconOffset", 20);
      // UIManager.put("MenuItem.afterCheckIconGap", 20);
      UIManager.put("MenuItem.minimumTextOffset", 20 + 20 + 31 - 9)

      UIManager.put("CheckBoxMenuItem.afterCheckIconGap", 20)
      UIManager.put("CheckBoxMenuItem.checkIconOffset", 20)
      // UIManager.put("CheckBoxMenuItem.minimumTextOffset", 100);

      val menuBar = JMenuBar()
      val menu = makeMenu("JMenu")
      menuBar.add(menu)

      menu.add(makeMenu("JMenu 1"))
      menu.add(makeMenu("JMenu 2"))

      menuBar.add(menu)
      menuBar.add(makeMenu("JMenu 3"))
      return menuBar
    }

    fun makeMenu(title: String): JMenu {
      val menu = JMenu(title)
      menu.add(JMenuItem("JMenuItem 1"))
      menu.add(JMenuItem("JMenuItem 2"))
      menu.add(JCheckBoxMenuItem("JCheckBoxMenuItem 1"))
      menu.add(JCheckBoxMenuItem("JCheckBoxMenuItem 2"))

      val rbmi1 = JRadioButtonMenuItem("JRadioButtonMenuItem 1")
      val rbmi2 = JRadioButtonMenuItem("JRadioButtonMenuItem 2")
      val bg = ButtonGroup()
      bg.add(rbmi1)
      bg.add(rbmi2)
      menu.add(rbmi1)
      menu.add(rbmi2)
      return menu
    }
  }
}

fun main() {
  EventQueue.invokeLater {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
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
      setJMenuBar(MainPanel.createMenuBar())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
