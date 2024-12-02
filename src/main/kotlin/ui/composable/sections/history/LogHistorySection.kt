package ui.composable.sections.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.model.items.DeviceItem
import data.repository.log.LogHistorySource
import data.repository.name.log.LogNameSource
import kotlinx.coroutines.launch
import notifications.InfoManagerData
import ui.application.WindowExtra
import ui.application.WindowStateManager
import ui.application.navigation.WindowData
import ui.composable.elements.DividerColored
import ui.composable.elements.ListWithScrollbar
import ui.composable.elements.history.NameCard
import ui.composable.elements.iconButtons.TooltipIconButton
import ui.composable.screens.LogHistoryDetailsScreen
import utils.Colors.transparentTextFieldDefault
import utils.EMPTY_STRING
import utils.getStringResource

@Composable
fun LogHistorySection(
    logNameSource: LogNameSource,
    windowStateManager: WindowStateManager,
    logHistorySource: LogHistorySource,
    onMessage: (InfoManagerData) -> Unit,
    deviceItem: DeviceItem = DeviceItem.emptyDeviceItem,
    onFilterClear: () -> Unit,
) {

    val scope = rememberCoroutineScope()
    val nameItems by logNameSource.by(context = scope.coroutineContext).collectAsState(initial = emptyList())
    var deleteInProgress by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(EMPTY_STRING) }

    val filteredNames = nameItems.filter { nameItem ->
        val matchesSearchText = searchText.isEmpty() ||
                nameItem.name.contains(searchText, ignoreCase = true) ||
                nameItem.sessionUuid.toString().contains(searchText, ignoreCase = true)

        val matchesSerialNumber = deviceItem.serialNumber.isEmpty() ||
                nameItem.deviceSerialNumber.contains(deviceItem.serialNumber, ignoreCase = true)

        matchesSearchText && matchesSerialNumber
    }

    Column(
        modifier = Modifier
            .padding(
                horizontal = 4.dp,
                vertical = 16.dp
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            deviceItem.let {
                if (it.serialNumber.isNotEmpty()) {

                    Text(
                        text = "$it (${it.serialNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    TooltipIconButton(
                        icon = Icons.Default.Close,
                        tooltip = getStringResource("info.clear.filter"),
                        function = onFilterClear
                    )
                }
            }

            TextField(
                value = searchText,
                colors = transparentTextFieldDefault,
                singleLine = true,
                onValueChange = {
                    searchText = it
                },
                placeholder = { Text(getStringResource("info.search")) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        DividerColored()

        ListWithScrollbar(
            lazyModifier = Modifier.padding(top = 8.dp),
            content = {
                items(filteredNames.reversed()) { nameItem ->
                    NameCard(
                        name = nameItem.name,
                        uuid = nameItem.sessionUuid,
                        dateTime = nameItem.dateTime,
                        onClick = {
                            windowStateManager.windowState?.openNewWindow?.let { newWindow ->
                                newWindow(
                                    WindowData(
                                        title = "${nameItem.name} (${nameItem.deviceSerialNumber})",
                                        icon = Icons.Default.Info,
                                        windowExtra = WindowExtra(
                                            screen = {
                                                val logs by logHistorySource.by(
                                                    context = scope.coroutineContext,
                                                    sessionUuid = nameItem.sessionUuid
                                                ).collectAsState(initial = emptyList())

                                                LogHistoryDetailsScreen(
                                                    logs = logs
                                                )
                                            },
                                            onClose = {}
                                        )
                                    )
                                )
                            }
                        },
                        onDelete = {
                            deleteInProgress = true
                            scope.launch {
                                logHistorySource.deleteBy(nameItem.sessionUuid)
                                logNameSource.deleteBy(nameItem.sessionUuid)
                                deleteInProgress = false
                                onMessage(
                                    InfoManagerData(
                                        message = getStringResource("info.delete.log.message")
                                    )
                                )
                            }
                        },
                        onUpdate = {
                            logNameSource.update(
                                sessionUuid = nameItem.sessionUuid,
                                name = it
                            )
                            onMessage(
                                InfoManagerData(
                                    message = getStringResource("info.name.update.log.message")
                                )
                            )
                        },
                        buttonsEnabled = !deleteInProgress,
                    )
                }
            }
        )
    }
}