package xyz.wingio.logra.utils.logcat

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.wingio.logra.domain.logcat.LogcatEntry
import xyz.wingio.logra.domain.manager.PreferenceManager
import xyz.wingio.logra.utils.Logger
import java.io.BufferedReader
import kotlin.concurrent.thread

object LogcatManager : KoinComponent {

    private const val command = "logcat -v epoch"
    private val prefs: PreferenceManager by inject()

    private lateinit var reader: BufferedReader
    private lateinit var proc: Process

    fun connect() {
        if (prefs.dumpLogs) Runtime.getRuntime().exec("logcat -c").waitFor()
        proc = Runtime.getRuntime().exec(command)
        reader = proc.inputStream.bufferedReader()
    }

    fun listen(callback: (LogcatEntry) -> Unit) {
        thread(start = true) {
            while (true) {
                if (!proc.isAlive) {
                    connect(); continue
                }
                reader.forEachLine {
                    val ent = LogcatEntry.fromLine(it)
                    ent?.let(callback)
                }
            }
        }
    }

    suspend fun getLogsFromPid(pid: Int): List<LogcatEntry> = withContext(Dispatchers.IO) {
        val logger = Logger("Exporter")
        logger.debug("Getting logs for $pid...")
        if(Build.VERSION.SDK_INT >= 24) {
            val logs = mutableListOf<LogcatEntry>()

            logger.debug("Running logcat command...")
            val process = Runtime
                .getRuntime()
                .exec("logcat -v epoch -d --pid=${pid}")
                .apply { waitFor() }

            if (process.exitValue() == 0) {
                logger.debug("Reading command output...")
                process.inputStream.bufferedReader().forEachLine {
                    LogcatEntry.fromLine(it)?.let { log ->
                        logs.add(log)
                    }
                }
            } else {
                logger.error("Failed to get logs")
            }

            logs
        } else {
            //TODO: Fallback method for devices running Android 6 and lower
            emptyList()
        }
    }

}