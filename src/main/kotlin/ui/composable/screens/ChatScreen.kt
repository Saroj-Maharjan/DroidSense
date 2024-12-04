package ui.composable.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import data.model.items.AiNameItem
import notifications.InfoManager
import ui.composable.elements.window.Sources
import ui.composable.sections.ai.ChatSection
import ui.composable.sections.info.InfoSection
import utils.DATABASE_DATETIME
import utils.getTimeStamp
import java.util.UUID

@Composable
fun ChatScreen(
    sources: Sources,
    uuid: UUID? = null,
) {

    val infoManager = remember { InfoManager() }
    val scope = rememberCoroutineScope()
    val sessionUuid by remember { mutableStateOf(uuid ?: UUID.randomUUID()) }

    LaunchedEffect(Unit) {
        if(uuid == null) {
            sources.aiNameSource.add(
                AiNameItem(
                    sessionUuid = sessionUuid,
                    name = "Test12",
                    dateTime = getTimeStamp(DATABASE_DATETIME),
                    deviceSerialNumber = ""
                )
            )
        }
    }

    Column {
        InfoSection(
            onCloseClicked = { infoManager.clearInfoMessage() },
            message = infoManager.infoManagerData.value.message,
            color = infoManager.infoManagerData.value.color
        )

        ChatSection(
            sources = sources,
            uuid = sessionUuid,
            onMessage = {
                infoManager.showMessage(
                    infoManagerData = it,
                    scope = scope
                )
            }
        )
    }
}