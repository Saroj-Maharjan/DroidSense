package ui.composable.sections.settings.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import data.keys.AIKey
import data.repository.settings.SettingsSource
import notifications.InfoManagerData
import ui.composable.elements.OutlinedText
import ui.composable.elements.SettingRow
import utils.getStringResource

@Composable
fun Ollama(settingsSource: SettingsSource, onMessage: (InfoManagerData) -> Unit) {

    var ollamaURL by remember { mutableStateOf("") }

    Column {
        SettingRow(
            key = AIKey.OLLAMA,
            settingsSource = settingsSource,
            hintText = getStringResource("info.ollama.url.hint"),
            saveTooltip = getStringResource("info.save.ollama.url"),
            enableTooltip = getStringResource("info.enable.ollama.url"),
            removeTooltip = getStringResource("info.disable.ollama.url"),
            editMessage = getStringResource("info.message.edit.ollama.url"),
            removeMessage = getStringResource("info.message.remove.ollama.url"),
            onMessage = onMessage,
            onKeyFound = { key ->
                ollamaURL = key
            }
        )

        OutlinedText(
            modifier = Modifier.weight(1f),
            readOnly = true,
            enabled = ollamaURL.trim().isNotEmpty(),
            text = "TextTes",
            hintText = getStringResource("info.database.location"),
            onValueChanged = {}
        )
    }
}
