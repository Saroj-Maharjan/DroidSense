package log

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import notifications.InfoManagerData
import utils.APK_EXTENSION
import utils.Colors.darkBlue
import utils.Colors.darkRed
import utils.EMPTY_STRING
import utils.getStringResource
import utils.pickFile

suspend fun uninstallApp(
    adbPath: String,
    packageName: String
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val process = ProcessBuilder(adbPath, "shell", "pm", "uninstall", packageName).start()
        val isSuccess = process.inputStream.bufferedReader().useLines { lines ->
            lines.any { it.contains("Success") }
        }

        if (isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to uninstall the app"))
        }
    }.getOrElse { Result.failure(it) }
}

suspend fun clearAppCache(
    adbPath: String,
    packageName: String
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val process = ProcessBuilder(adbPath, "shell", "pm", "clear", packageName).start()
        val isSuccess = process.inputStream.bufferedReader().useLines { lines ->
            lines.any { it.contains("Success") }
        }

        if (isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to clear the app cache"))
        }
    }.getOrElse { Result.failure(it) }
}

suspend fun installApplication(
    adbPath: String,
    identifier: String,
    spaceId: String
): Result<InfoManagerData> = withContext(Dispatchers.Default) {
    runCatching {
        val file = pickFile(allowedExtension = APK_EXTENSION) ?: return@withContext Result.failure(
            IllegalArgumentException("File selection was canceled.")
        )

        val command = buildAdbInstallCommand(
            adbPath = adbPath,
            identifier = identifier,
            filePath = file.absolutePath,
            spaceId = spaceId
        )
        val process = ProcessBuilder(command).apply { redirectErrorStream(true) }.start()
        val exitCode = process.waitFor()

        val resultMessage = if (exitCode == 0) {
            getStringResource("success.file.install")
        } else {
            getStringResource("error.file.install")
        }

        Result.success(
            InfoManagerData(
                message = "$resultMessage: ${file.canonicalPath}",
                color = if (exitCode == 0) darkBlue else darkRed
            )
        )
    }.getOrElse {
        Result.failure(Exception(getStringResource("error.file.install")))
    }
}


private fun buildAdbInstallCommand(
    adbPath: String,
    identifier: String,
    filePath: String,
    spaceId: String
): List<String> {
    return mutableListOf(adbPath, "-s", identifier, "install").apply {
        add("--user")
        add(spaceId)

        addAll(listOf("-r", filePath))
    }
}

fun getPrivateSpaceId(adbPath: String, identifier: String): String? {
    return getAvailableSpaces(adbPath, identifier)
        .firstOrNull { it.contains("PrivateSpace") }
        ?.substringAfter("UserInfo{")
        ?.substringBefore(":")
}

fun getAvailableSpaces(adbPath: String, identifier: String): List<String> {
    return runCatching {
        ProcessBuilder(adbPath, "-s", identifier, "shell", "pm", "list", "users").start().inputStream
            .bufferedReader()
            .use { reader ->
                reader.readLines()
            }
    }.getOrNull()?.mapNotNull { line ->
        if (line.contains("UserInfo")) {
            line.trim()
        } else {
            null
        }
    } ?: emptyList()
}

fun getDeviceProperty(
    adbPath: String,
    identifier: String,
    property: String
): String {
    return runCatching {
        ProcessBuilder(adbPath, "-s", identifier, "shell", property).start().inputStream
            .bufferedReader()
            .use { reader ->
                reader.readLine()
            }
    }.getOrNull()?.trim() ?: EMPTY_STRING
}

fun getDevicePropertyList(
    adbPath: String,
    identifier: String,
    property: String,
    startingItem: String? = null
): List<String> {
    return runCatching {
        val process = ProcessBuilder(adbPath, "-s", identifier, "shell", property).start()
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