package abkabk.azbarkon.features.profile

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.model.profile.BadgeUi
import abkabk.azbarkon.domain.model.profile.GameLevelCatalog
import abkabk.azbarkon.domain.model.profile.LevelListItemUi
import abkabk.azbarkon.domain.model.profile.LevelRowState
import abkabk.azbarkon.domain.model.profile.ProfileSheet
import abkabk.azbarkon.features.profile.widget.rememberWidgetPickerLauncher
import abkabk.azbarkon.ui.components.SarvModalBottomSheet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.add_box_24px
import sarv.shared.generated.resources.check_circle
import sarv.shared.generated.resources.download
import sarv.shared.generated.resources.lock
import sarv.shared.generated.resources.notifications
import sarv.shared.generated.resources.notifications_outlined
import sarv.shared.generated.resources.profile_add_widget
import sarv.shared.generated.resources.profile_add_widget_subtitle
import sarv.shared.generated.resources.profile_badges_title
import sarv.shared.generated.resources.profile_daily_distich_subtitle
import sarv.shared.generated.resources.profile_daily_distich_title
import sarv.shared.generated.resources.profile_export_data
import sarv.shared.generated.resources.profile_export_data_subtitle
import sarv.shared.generated.resources.profile_font_size_big
import sarv.shared.generated.resources.profile_font_size_bigger
import sarv.shared.generated.resources.profile_font_size_default
import sarv.shared.generated.resources.profile_font_size_title
import sarv.shared.generated.resources.profile_import_data
import sarv.shared.generated.resources.profile_import_data_subtitle
import sarv.shared.generated.resources.profile_level_format
import sarv.shared.generated.resources.profile_level_score_required
import sarv.shared.generated.resources.profile_level_start
import sarv.shared.generated.resources.profile_levels_title
import sarv.shared.generated.resources.profile_memorization_reminder_subtitle
import sarv.shared.generated.resources.profile_memorization_reminder_title
import sarv.shared.generated.resources.profile_remote_notification_subtitle
import sarv.shared.generated.resources.profile_remote_notification_title
import sarv.shared.generated.resources.profile_settings_title
import sarv.shared.generated.resources.profile_theme_dark
import sarv.shared.generated.resources.profile_theme_light
import sarv.shared.generated.resources.profile_theme_system
import sarv.shared.generated.resources.profile_theme_title
import sarv.shared.generated.resources.upload

private const val FONT_SIZE_DEFAULT = 1f
private const val FONT_SIZE_BIG = 1.1f
private const val FONT_SIZE_BIGGER = 1.2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheets(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheet = state.activeSheet ?: return
    val openWidgetPicker = rememberWidgetPickerLauncher()
    val onAddWidgetClick = openWidgetPicker?.let { picker ->
        {
            onAction(ProfileAction.OnDismissSheet)
            picker()
        }
    }

    SarvModalBottomSheet(
        onDismissRequest = { onAction(ProfileAction.OnDismissSheet) },
        modifier = modifier,
    ) {
        when (sheet) {
            ProfileSheet.Settings ->
                ProfileSettingsSheetContent(
                    isDailyDistichEnabled = state.isDailyDistichNotificationEnabled,
                    isMemorizationReminderEnabled = state.isMemorizationReminderEnabled,
                    isRemoteNotificationGranted = state.isRemoteNotificationGranted,
                    themeMode = state.themeMode,
                    fontSizeScale = state.fontSizeScale,
                    onDailyDistichToggle = { onAction(ProfileAction.OnDailyDistichNotificationToggle(it)) },
                    onMemorizationReminderToggle = { onAction(ProfileAction.OnMemorizationReminderToggle(it)) },
                    onRemoteNotificationClick = { onAction(ProfileAction.OnRemoteNotificationClick) },
                    onThemeModeSelect = { onAction(ProfileAction.OnThemeModeSelected(it)) },
                    onFontSizeScaleSelect = { onAction(ProfileAction.OnFontSizeScaleSelected(it)) },
                    onAddWidgetClick = onAddWidgetClick,
                    onExportData = onExportData,
                    onImportData = onImportData,
                )

            ProfileSheet.Badges ->
                ProfileBadgesSheetContent(badges = state.allBadges)

            ProfileSheet.Levels ->
                ProfileLevelsSheetContent(levels = state.allLevels)
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ProfileSettingsSheetContent(
    isDailyDistichEnabled: Boolean,
    isMemorizationReminderEnabled: Boolean,
    isRemoteNotificationGranted: Boolean,
    themeMode: ThemeMode,
    fontSizeScale: Float,
    onDailyDistichToggle: (Boolean) -> Unit,
    onMemorizationReminderToggle: (Boolean) -> Unit,
    onRemoteNotificationClick: () -> Unit,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onFontSizeScaleSelect: (Float) -> Unit,
    onAddWidgetClick: (() -> Unit)?,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LocalSarvDimensions.current.dimen16)
                .padding(bottom = LocalSarvDimensions.current.dimen24),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.profile_settings_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        ProfileSettingToggleRow(
            title = stringResource(Res.string.profile_daily_distich_title),
            subtitle = stringResource(Res.string.profile_daily_distich_subtitle),
            checked = isDailyDistichEnabled,
            onCheckedChange = onDailyDistichToggle,
        )


        ProfileSettingToggleRow(
            title = stringResource(Res.string.profile_memorization_reminder_title),
            subtitle = stringResource(Res.string.profile_memorization_reminder_subtitle),
            checked = isMemorizationReminderEnabled,
            onCheckedChange = onMemorizationReminderToggle,
        )

        ProfileDataActionRow(
            icon = painterResource(Res.drawable.notifications_outlined),
            iconFilled = painterResource(Res.drawable.notifications),
            filled = isRemoteNotificationGranted,
            title = stringResource(Res.string.profile_remote_notification_title),
            subtitle = stringResource(Res.string.profile_remote_notification_subtitle),
            onClick = onRemoteNotificationClick,
        )

        if (onAddWidgetClick != null) {
            ProfileDataActionRow(
                icon = painterResource(Res.drawable.add_box_24px),
                title = stringResource(Res.string.profile_add_widget),
                subtitle = stringResource(Res.string.profile_add_widget_subtitle),
                onClick = onAddWidgetClick,
            )
        }

        ProfileDataActionRow(
            icon = painterResource(Res.drawable.upload),
            title = stringResource(Res.string.profile_export_data),
            subtitle = stringResource(Res.string.profile_export_data_subtitle),
            onClick = onExportData,
        )

        ProfileDataActionRow(
            icon = painterResource(Res.drawable.download),
            title = stringResource(Res.string.profile_import_data),
            subtitle = stringResource(Res.string.profile_import_data_subtitle),
            onClick = onImportData,
        )

        ProfileThemeSelector(themeMode, onThemeModeSelect)

        ProfileFontSizeSelector(fontSizeScale, onFontSizeScaleSelect)
    }
}

@Composable
private fun ProfileThemeSelector(
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(Res.string.profile_theme_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            ProfileSegmentedOption(
                label = stringResource(Res.string.profile_theme_system),
                selected = themeMode == ThemeMode.System,
                onClick = { onThemeModeSelect(ThemeMode.System) },
                modifier = Modifier.weight(1f),
            )
            ProfileSegmentedOption(
                label = stringResource(Res.string.profile_theme_light),
                selected = themeMode == ThemeMode.Light,
                onClick = { onThemeModeSelect(ThemeMode.Light) },
                modifier = Modifier.weight(1f),
            )
            ProfileSegmentedOption(
                label = stringResource(Res.string.profile_theme_dark),
                selected = themeMode == ThemeMode.Dark,
                onClick = { onThemeModeSelect(ThemeMode.Dark) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProfileFontSizeSelector(
    fontSizeScale: Float,
    onFontSizeScaleSelect: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(Res.string.profile_font_size_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            ProfileSegmentedOption(
                label = stringResource(Res.string.profile_font_size_default),
                selected = fontSizeScale == FONT_SIZE_DEFAULT,
                onClick = { onFontSizeScaleSelect(FONT_SIZE_DEFAULT) },
                modifier = Modifier.weight(1f),
            )
            ProfileSegmentedOption(
                label = stringResource(Res.string.profile_font_size_big),
                selected = fontSizeScale == FONT_SIZE_BIG,
                onClick = { onFontSizeScaleSelect(FONT_SIZE_BIG) },
                modifier = Modifier.weight(1f),
            )
            ProfileSegmentedOption(
                label = stringResource(Res.string.profile_font_size_bigger),
                selected = fontSizeScale == FONT_SIZE_BIGGER,
                onClick = { onFontSizeScaleSelect(FONT_SIZE_BIGGER) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProfileSettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    checkedBorderColor = MaterialTheme.colorScheme.tertiary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.tertiary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.tertiary,
                )
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun ProfileDataActionRow(
    icon: Painter,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconFilled: Painter = icon,
    filled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            painter = if (filled) iconFilled else icon,
            contentDescription = null,
            tint =
                if (filled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun ProfileSegmentedOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ).clickable(onClick = onClick)
                .padding(horizontal = LocalSarvDimensions.current.dimen8, vertical = LocalSarvDimensions.current.dimen8),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )
    }
}

@Composable
private fun ProfileBadgesSheetContent(badges: List<BadgeUi>, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = LocalSarvDimensions.current.dimen16)
                .padding(bottom = LocalSarvDimensions.current.dimen24),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        Text(
            text = stringResource(Res.string.profile_badges_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(badges, key = { it.id }) { badge ->
                BadgeListRow(
                    item = badge,
                    modifier = Modifier.padding(vertical = LocalSarvDimensions.current.dimen12),
                )
                if (badge.id != badges.lastOrNull()?.id) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ProfileLevelsSheetContent(levels: List<LevelListItemUi>, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = LocalSarvDimensions.current.dimen16)
                .padding(bottom = LocalSarvDimensions.current.dimen24),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        Text(
            text = stringResource(Res.string.profile_levels_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
            items(levels, key = { it.level.id }) { item ->
                ProfileLevelRow(item = item)
            }
        }
    }
}

@Composable
private fun ProfileLevelRow(item: LevelListItemUi, modifier: Modifier = Modifier) {
    val backgroundColor =
        when (item.state) {
            LevelRowState.Current -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    val subtitle =
        if (item.level.id == 1) {
            stringResource(Res.string.profile_level_start)
        } else {
            stringResource(
                Res.string.profile_level_score_required,
                GameLevelCatalog.requiredScoreForLevel(item.level.id),
            )
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                .background(backgroundColor)
                .padding(horizontal = LocalSarvDimensions.current.dimen12,
                    vertical = LocalSarvDimensions.current.dimen16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {

        Text(
            text = stringResource(Res.string.profile_level_format, item.level.id),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = item.level.name,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }


        levelImageResource(item.level.id)?.let { drawable ->
            val isLocked = item.state == LevelRowState.Locked
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen40),
                colorFilter =
                    if (isLocked) {
                        ColorFilter.colorMatrix(
                            ColorMatrix().apply { setToSaturation(0f) },
                        )
                    } else {
                        null
                    },
                alpha = if (isLocked) 0.45f else 1f,
            )
        }

        when (item.state) {
            LevelRowState.Locked ->
                Icon(
                    painter = painterResource(Res.drawable.lock),
                    contentDescription = null,
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

            LevelRowState.Completed,
            LevelRowState.Current,
            ->
                Icon(
                    painter = painterResource(Res.drawable.check_circle),
                    contentDescription = null,
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
                    tint = MaterialTheme.colorScheme.primary,
                )
        }
    }
}
