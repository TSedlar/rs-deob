package me.sedlar.deobfuscator.osrs.game

import java.applet.Applet
import java.applet.AppletContext
import java.applet.AppletStub
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader


/**
 * @author Tyler Sedlar
 * @since 6/23/2018
 */
object Crawler {

    val OSRS_CONFIG = "http://oldschool6.runescape.com/jav_config.ws"

    private fun crawl(): Map<String, String> {
        val lines = URL(OSRS_CONFIG).readText().split("\n")
        val parameters: MutableMap<String, String> = HashMap()
        lines.forEach {
            var line = it
            if (line.startsWith("param=")) {
                line = line.substring(6)
            }
            val idx = line.indexOf("=")
            if (idx >= 0) {
                parameters[line.substring(0, idx)] = line.substring(idx + 1)
            }
        }
        return parameters
    }

    fun start(jar: File): Applet {
        val params = crawl()
        val classloader = URLClassLoader(arrayOf(jar.toURI().toURL()))
        val main = params["initial_class"]!!.replace(".class", "")
        val applet = classloader.loadClass(main).newInstance() as Applet
        applet.background = Color.BLACK
        applet.preferredSize = appletSizeOf(params)
        applet.size = applet.preferredSize
        applet.layout = null
        applet.setStub(createEnvironment(params, applet))
        applet.isVisible = true
        applet.init()
        return applet
    }

    private fun appletSizeOf(params: Map<String, String>): Dimension {
        return try {
            Dimension(Integer.parseInt(params["applet_minwidth"]), Integer.parseInt(params["applet_minheight"]))
        } catch (e: Exception) {
            Dimension(765, 503)
        }
    }

    private fun createEnvironment(params: Map<String, String>, applet: Applet): AppletStub {

        return object : AppletStub {
            override fun isActive(): Boolean {
                return true
            }

            override fun getDocumentBase(): URL? {
                return try {
                    URL(params["codebase"])
                } catch (e: MalformedURLException) {
                    null
                }

            }

            override fun getCodeBase(): URL? {
                return try {
                    URL(params["codebase"])
                } catch (e: MalformedURLException) {
                    null
                }

            }

            override fun getParameter(name: String): String? {
                return params[name]
            }

            override fun appletResize(width: Int, height: Int) {
                val size = Dimension(width, height)
                applet.size = size
            }

            override fun getAppletContext(): AppletContext? {
                return null
            }
        }
    }
}