import me.sedlar.deobfuscator.osrs.game.Crawler
import java.awt.GridLayout
import java.io.File
import javax.swing.JFrame

val JAR = "test.jar"
//val JAR = "D:/RuneScape/packs/oldschool/171.jar"

fun main(args: Array<String>) {

    val frame = JFrame()
    frame.layout = GridLayout(1, 0)

    val applet = Crawler.start(File(JAR))
    frame.add(applet)

    frame.pack()

    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
}