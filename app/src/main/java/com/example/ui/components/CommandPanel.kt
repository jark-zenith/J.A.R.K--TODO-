package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarkBackground
import com.example.ui.theme.JarkBlue
import com.example.ui.theme.JarkBlueContainer
import com.example.ui.theme.JarkBorder
import com.example.ui.theme.JarkBorderBright
import com.example.ui.theme.JarkGreen
import com.example.ui.theme.JarkRed
import com.example.ui.theme.JarkSurface
import com.example.ui.theme.JarkSurfaceVariant
import com.example.ui.theme.JarkTextMuted
import com.example.ui.theme.JarkTextPrimary
import com.example.ui.theme.JarkTextSecondary
import com.example.ui.viewmodel.CommandStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommandPanel(
    command: String,
    commandStatus: CommandStatus,
    commandReply: String,
    onCommandChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "What should I work on next?",
        "Add task Review sprint goals high priority",
        "What is due today?",
        "Complete task Map next J.A.R.K. milestone",
        "Show overdue tasks"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF101A25), Color(0xFF11161D))
                )
            )
            .border(1.dp, Color(0xFF274863), RoundedCornerShape(8.dp))
            .padding(18.dp)
            .testTag("command_panel")
    ) {
        Column {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF173049))
                        .border(1.dp, Color(0xFF275273), RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "J.A.R.K. Command",
                        tint = JarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "J.A.R.K. COMMAND LAYER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.1.sp
                        ),
                        color = JarkTextMuted
                    )
                    Text(
                        text = "Ask J.A.R.K. anything",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = JarkTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Turn intent into action. Type or speak natural commands to manage tasks.",
                style = MaterialTheme.typography.bodySmall,
                color = JarkTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Command Input Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = command,
                    onValueChange = onCommandChange,
                    placeholder = {
                        Text(
                            text = if (commandStatus == CommandStatus.LISTENING) "Listening..." else "What should I work on next?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (commandStatus == CommandStatus.LISTENING) JarkGreen else JarkTextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    singleLine = true,
                    enabled = commandStatus != CommandStatus.LOADING,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0B1219),
                        unfocusedContainerColor = Color(0xFF0B1219),
                        disabledContainerColor = Color(0xFF0B1219),
                        focusedBorderColor = JarkBlue,
                        unfocusedBorderColor = Color(0xFF2A4258),
                        focusedTextColor = JarkTextPrimary,
                        unfocusedTextColor = JarkTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSubmit() })
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Mic Button
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (commandStatus == CommandStatus.LISTENING) Color(0xFF16342C) else Color(0xFF141D27))
                        .border(1.dp, if (commandStatus == CommandStatus.LISTENING) JarkGreen else Color(0xFF2A4258), RoundedCornerShape(6.dp))
                        .testTag("voice_command_button")
                ) {
                    Icon(
                        imageVector = if (commandStatus == CommandStatus.LISTENING) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (commandStatus == CommandStatus.LISTENING) JarkGreen else JarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = onSubmit,
                    enabled = command.isNotBlank() && commandStatus != CommandStatus.LOADING,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (command.isNotBlank()) JarkBlue else Color(0xFF151C25))
                        .testTag("send_command_button")
                ) {
                    if (commandStatus == CommandStatus.LOADING) {
                        CircularProgressIndicator(
                            color = JarkBackground,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Execute command",
                            tint = if (command.isNotBlank()) JarkBackground else JarkTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Suggestion chips
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestions.forEach { suggestion ->
                    Surface(
                        color = Color(0xFF0F1822),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .border(1.dp, Color(0xFF1E2E3E), RoundedCornerShape(4.dp))
                            .clickable {
                                onCommandChange(suggestion)
                            }
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = JarkTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Command reply box
            AnimatedVisibility(
                visible = commandReply.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF142230))
                        .border(1.dp, Color(0xFF2E4D68), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("command_reply_box")
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(6.dp)
                                .background(JarkBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = commandReply,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFD3E7FA)
                        )
                    }
                }
            }
        }
    }
}
