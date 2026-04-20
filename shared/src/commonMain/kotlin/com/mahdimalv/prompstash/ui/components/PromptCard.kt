package com.mahdimalv.prompstash.ui.components

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mahdimalv.prompstash.data.model.Prompt

@Composable
fun PromptCard(
    prompt: Prompt,
    isPinned: Boolean,
    showPinAction: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentPadding = if (isPinned) 12.dp else 16.dp
    val bodyMaxLines = if (isPinned) 2 else 3
    val titleMaxLines = if (isPinned) 1 else 2
    val showMetadata = !isPinned
    val visibleTags = if (isPinned) prompt.tags.take(2) else prompt.tags

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                PromptTitle(
                    title = prompt.title,
                    maxLines = titleMaxLines,
                    modifier = Modifier.weight(1f),
                )
                if (showPinAction) {
                    IconButton(onClick = onPinToggle) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin prompt" else "Pin prompt",
                            tint = if (isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.rotate(if (isPinned) 0f else 35f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(if (isPinned) 4.dp else 8.dp))
            Text(
                text = prompt.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = bodyMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            if (showMetadata) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${prompt.wordCount} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (visibleTags.isNotEmpty() || showPinAction) {
                Spacer(Modifier.height(if (isPinned) 6.dp else 8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        visibleTags.forEach { tag ->
                            FilterChip(
                                selected = false,
                                onClick = onClick,
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                                border = null,
                            )
                        }
                    }
                    if (showPinAction) {
                        IconButton(onClick = onCopy) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy prompt",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptTitle(
    title: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
