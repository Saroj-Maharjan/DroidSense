package ui.composable.elements.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jskako.droidsense.generated.resources.Res
import com.jskako.droidsense.generated.resources.info_edit_message
import com.jskako.droidsense.generated.resources.info_try_again
import data.model.ai.AIItem
import data.model.ai.ollama.AiRole
import ui.composable.elements.MarkdownText
import ui.composable.elements.iconButtons.TooltipIconButton
import ui.composable.elements.window.EditDialog
import utils.Colors.darkBlue
import utils.Colors.darkRed
import utils.Colors.lightGray
import java.util.UUID

@Composable
fun ChatCard(
    aiItem: AIItem,
    onUpdate: (UUID, String) -> Unit,
    onTryAgain: (UUID) -> Unit,
    buttonsEnabled: Boolean = true
) {

    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditDialog(
            title = Res.string.info_edit_message,
            text = aiItem.message,
            singleLine = false,
            onConfirmRequest = {
                showEditDialog = false
                onUpdate(aiItem.messageUUID, it)
            },
            onDismissRequest = {
                showEditDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (aiItem.role == AiRole.USER) Color.White else lightGray
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row {
                MarkdownText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .weight(1f),
                    text = aiItem.message,
                )

                if (aiItem.role == AiRole.USER) {
                    TooltipIconButton(
                        isEnabled = buttonsEnabled,
                        tint = if (buttonsEnabled) darkBlue else lightGray,
                        icon = Icons.Default.Edit,
                        tooltip = Res.string.info_edit_message,
                        function = {
                            showEditDialog = true
                        }
                    )
                }

                if (!aiItem.succeed) {
                    TooltipIconButton(
                        isEnabled = buttonsEnabled,
                        tint = darkRed,
                        icon = Icons.Default.Repeat,
                        tooltip = Res.string.info_try_again,
                        function = {
                            onTryAgain(aiItem.messageUUID)
                        }
                    )
                }
            }

            addSpaceHeight(8.dp)

            Text(
                text = "${aiItem.dateTime} - ${aiItem.url} ${aiItem.model}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun addSpaceHeight(height: Dp = 5.dp) {
    Spacer(modifier = Modifier.height(height))
}