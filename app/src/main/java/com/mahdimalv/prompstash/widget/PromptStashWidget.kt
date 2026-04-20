package com.mahdimalv.prompstash.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mahdimalv.prompstash.AndroidAppSingletons
import com.mahdimalv.prompstash.MainActivity
import com.mahdimalv.prompstash.R
import kotlinx.coroutines.flow.first

internal class PromptStashWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = PromptStashWidgetDataStore.load(context)
        provideContent {
            PromptStashWidgetContent(widgetData)
        }
    }
}

class PromptStashWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PromptStashWidget()
}

internal object PromptStashWidgetUpdater {
    private const val UPDATE_WORK_NAME = "prompt_stash_widget_update"

    fun enqueue(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            UPDATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<PromptStashWidgetUpdateWorker>().build(),
        )
    }
}

class PromptStashWidgetUpdateWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        PromptStashWidget().updateAll(applicationContext)
        return Result.success()
    }
}

class CopyPromptAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val promptBody = parameters[PromptBodyKey].orEmpty()
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Prompt", promptBody))
    }

    companion object {
        val PromptBodyKey = ActionParameters.Key<String>("prompt_body")
    }
}

@Composable
private fun PromptStashWidgetContent(widgetData: PromptStashWidgetData) {
    val size = LocalSize.current
    val visibleEntries = widgetData.entries.take(maxVisibleEntries(size.height.value))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1B1B1F)))
            .padding(12.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = "PromptStash",
            modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
            style = TextStyle(
                color = ColorProvider(day = Color(0xFF111318), night = Color(0xFFE3E2E6)),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (visibleEntries.isEmpty()) {
            Text(
                text = "Pin prompts in the app",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF5E5F63), night = Color(0xFFC6C6CA)),
                    fontSize = 14.sp,
                ),
                maxLines = 2,
            )
        } else {
            visibleEntries.forEachIndexed { index, entry ->
                PromptEntryRow(entry)
                if (index != visibleEntries.lastIndex) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun PromptEntryRow(entry: PromptStashWidgetEntry) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = entry.title,
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(actionStartActivity<MainActivity>()),
            style = TextStyle(
                color = ColorProvider(day = Color(0xFF111318), night = Color(0xFFE3E2E6)),
                fontSize = 18.sp,
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.width(2.dp))
        Box(
            modifier = GlanceModifier
                .size(18.dp)
                .clickable(
                    actionRunCallback<CopyPromptAction>(
                        actionParametersOf(CopyPromptAction.PromptBodyKey to entry.body)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_copy),
                contentDescription = "Copy prompt",
                modifier = GlanceModifier.size(16.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private fun maxVisibleEntries(heightDp: Float): Int = when {
    heightDp < 96f -> 1
    heightDp < 138f -> 2
    else -> 3
}

private data class PromptStashWidgetData(
    val entries: List<PromptStashWidgetEntry>,
)

private data class PromptStashWidgetEntry(
    val title: String,
    val body: String,
)

private object PromptStashWidgetDataStore {
    private const val MAX_PINNED_PROMPTS = 3
    private const val PINNED_PROMPT_IDS_SEPARATOR = "\n"
    private val pinnedPromptIdsKey = stringPreferencesKey("pinned_prompt_ids")

    suspend fun load(context: Context): PromptStashWidgetData {
        val appContext = context.applicationContext
        val preferences = AndroidAppSingletons.preferencesDataStore(appContext).data.first()
        val pinnedPromptIds = preferences[pinnedPromptIdsKey]
            .orEmpty()
            .split(PINNED_PROMPT_IDS_SEPARATOR)
            .map(String::trim)
            .filter(String::isNotBlank)
            .take(MAX_PINNED_PROMPTS)

        if (pinnedPromptIds.isEmpty()) {
            return PromptStashWidgetData(entries = emptyList())
        }

        val promptsById = AndroidAppSingletons.promptDatabase(appContext)
            .promptDao()
            .getAllPromptEntities()
            .asSequence()
            .filter { it.deletedAt == null }
            .associateBy { it.id }

        return PromptStashWidgetData(
            entries = pinnedPromptIds.mapNotNull { promptId ->
                promptsById[promptId]?.let { prompt ->
                    PromptStashWidgetEntry(
                        title = prompt.title,
                        body = prompt.body,
                    )
                }
            }
        )
    }
}
