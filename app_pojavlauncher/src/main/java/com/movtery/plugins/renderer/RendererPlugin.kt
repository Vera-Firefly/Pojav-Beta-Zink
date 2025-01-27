package com.movtery.plugins.renderer

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.R

/**
 * FCL、ZalithLauncher, PGW 等渲染器插件
 * [FCL Renderer Plugin](https://github.com/FCL-Team/FCLRendererPlugin)
 */
object RendererPlugin {
    data class Renderer(
        val id: String,
        val des: String,
        val glName: String,
        val eglName: String,
        val path: String,
        val env: List<Pair<String, String>>
    )

    private var isInitialized: Boolean = false
    private const val PACKAGE_FLAGS = PackageManager.GET_META_DATA or PackageManager.GET_SHARED_LIBRARY_FILES

    @JvmStatic
    fun isInitialized() = isInitialized

    @JvmStatic
    private val rendererList: MutableList<Renderer> = mutableListOf()

    @JvmStatic
    fun getRendererList() = ArrayList(rendererList)

    @JvmStatic
    val selectedRenderer: Renderer?
        get() {
            return getRendererList().find { it.id == Tools.LOCAL_RENDERER }
        }

    @JvmStatic
    fun initRenderers(context: Context) {
        rendererList.clear()

        val queryIntentActivities = context.packageManager.queryIntentActivities(Intent("android.intent.action.MAIN"), PACKAGE_FLAGS)
        queryIntentActivities.forEach {
            val activityInfo = it.activityInfo
            //尝试进行解析渲染器插件
            parsePlugin(context, it.activityInfo.applicationInfo)
        }
        isInitialized = true
    }

    @JvmStatic
    fun isAvailable(): Boolean {
        return rendererList.isNotEmpty()
    }

    /**
     * 解析 ZalithLauncher、FCL 渲染器插件
     */
    private fun parsePlugin(context: Context, info: ApplicationInfo) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (
                metaData.getBoolean("fclPlugin", false) ||
                metaData.getBoolean("zalithRendererPlugin", false) ||
                metaData.getBoolean("pgwRendererPlugin", false)
            ) {
                val rendererString = metaData.getString("renderer") ?: return
                val des = metaData.getString("des") ?: return
                val pojavEnvString = metaData.getString("pojavEnv") ?: return
                val nativeLibraryDir = info.nativeLibraryDir
                val renderer = rendererString.split(":")
                val pojavEnvPair = pojavEnvString.split(":").run {
                    val envPairList = mutableListOf<Pair<String, String>>()
                    forEach { envString ->
                        if (envString.contains("=")) {
                            val stringList = envString.split("=")
                            envPairList.add(Pair(stringList[0], stringList[1]))
                        }
                    }
                    envPairList
                }
                rendererList.add(
                    Renderer(
                        pojavEnvPair.find { it.first == "POJAV_RENDERER" }?.second ?: renderer[0],
                        "$des (${
                            context.getString(
                                R.string.setting_renderer_from_plugins,
                                runCatching {
                                    context.packageManager.getApplicationLabel(info)
                                }.getOrElse {
                                    context.getString(R.string.generic_unknown)
                                }
                            )
                        })",
                        renderer[1],
                        renderer[2],
                        nativeLibraryDir,
                        pojavEnvPair
                    )
                )
            }
        }
    }
}