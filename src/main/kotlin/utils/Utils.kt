package utils

import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.ResourceBundle
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import notifications.InfoManagerData
import settitngs.GlobalVariables
import settitngs.GlobalVariables.adbPath
import utils.Colors.darkRed

fun getResourceBundle(baseName: String): ResourceBundle = ResourceBundle.getBundle(baseName)
fun getStringResource(resourceName: String) =
    getResourceBundle(STRING_RESOURCES).getString(resourceName) ?: EMPTY_STRING

fun getTimeStamp(format: String = DEFAULT_TIMESTAMP): String = DateTimeFormatter
    .ofPattern(format)
    .withZone(ZoneOffset.UTC)
    .format(LocalDateTime.now())

fun openFile(path: String): InfoManagerData {
    val result = runCatching {
        val file = File(path)
        check(Desktop.isDesktopSupported()) { getStringResource("error.openfile.desktop.unsupported") }
        check(file.exists()) { getStringResource("error.openfile.no.file") }
        Desktop.getDesktop().open(file)
    }

    return if (result.isSuccess) {
        InfoManagerData(
            message = "${getStringResource("success.openfile.general")}: $path"
        )
    } else {
        InfoManagerData(
            message = "${getStringResource("error.openfile.cannot.open")} $path\n${result.exceptionOrNull()}"
        )
    }
}

fun pickDirectoryDialog(): String? {
    val fileDialog = FileDialog(Frame(), getStringResource("info.directory.general"), FileDialog.LOAD).apply {
        mode = FileDialog.SAVE
        isMultipleMode = false
        isVisible = true
    }

    return if (!fileDialog.directory.isNullOrEmpty() && !fileDialog.file.isNullOrEmpty()) {
        "${fileDialog.directory}/${fileDialog.file}"
    } else {
        null
    }
}

fun pickFile(allowedExtension: String? = null): File? {
    val fileDialog = FileDialog(null as Frame?, "Choose File", FileDialog.LOAD)
    fileDialog.isVisible = true
    fileDialog.isMultipleMode = false

    val selectedFile = File(fileDialog.directory, fileDialog.file)
    return selectedFile.takeIf { allowedExtension?.equals(it.extension, ignoreCase = true) != false }
}

fun getUserOS(): String {
    val osName = System.getProperty(SYSTEM_OS_PROPERTY).lowercase(Locale.getDefault())
    return OS.entries.firstOrNull { osName.contains(it.osName(), ignoreCase = true) }
        ?.osName() ?: OS.UNSUPPORTED.osName()
}

fun isSoftwareInstalled(software: String) = ProcessBuilder("which", software).start().waitFor() == 0

fun getImageBitmap(path: String) = useResource(path) { loadImageBitmap(it) }

fun startScrCpy(serialNumber: String): Process =
    ProcessBuilder(GlobalVariables.scrCpyPath.value, "-s", serialNumber).start()

fun getDeviceProperty(serialNumber: String, property: String): String {
    return runCatching {
        ProcessBuilder(adbPath.value, "-s", serialNumber, "shell", property).start().inputStream
            .bufferedReader()
            .use { reader ->
                reader.readLine()
            }
    }.getOrNull()?.trim() ?: EMPTY_STRING
}

fun getDevicePropertyList(serialNumber: String, property: String, startingItem: String? = null): List<String> {
    return runCatching {
        val process = ProcessBuilder(adbPath.value, "-s", serialNumber, "shell", property).start()
        val lines = process.inputStream
            .bufferedReader()
            .lineSequence()
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:") }
            .toList()
            .sorted()
        process.waitFor()
        return (startingItem?.let {
            mutableListOf(it).apply {
                addAll(lines)
            }
        } ?: lines)
    }.getOrElse {
        it.printStackTrace()
        emptyList()
    }
}

suspend fun installApplication(
    serialNumber: String,
    onMessage: (InfoManagerData) -> Unit
) {
    return withContext(Default) {
        runCatching {
            pickFile(allowedExtension = APK_EXTENSION)?.let { file ->
                onMessage(
                    InfoManagerData(
                        message = "${getStringResource("info.file.install")}: ${file.name}",
                        duration = null
                    )
                )
                val command = "${adbPath.value} -s $serialNumber install -r ${file.absolutePath}"
                val process = Runtime.getRuntime().exec(command)

                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    onMessage(
                        InfoManagerData(
                            message = "${getStringResource("success.file.install")}: ${file.canonicalPath}"
                        )
                    )
                } else {
                    onMessage(
                        InfoManagerData(
                            message = "${getStringResource("error.file.install")}: ${file.canonicalPath}",
                            color = darkRed
                        )
                    )
                }
            }
        }.getOrElse {
            onMessage(
                InfoManagerData(
                    message = getStringResource("error.file.install"),
                    color = darkRed
                )
            )
        }
    }
}