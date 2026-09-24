package com.dailycompany.feature.alarms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dailycompany.ui.components.SectionHeader
import com.dailycompany.ui.theme.Spacing
import com.dailycompany.ui.theme.strings

/**
 * Ajuda para quando os alarmes não disparam (`permissions_help`): explica as
 * três causas (notificações, alarmes exatos, bateria) com atalhos diretos
 * para as definições do sistema.
 */
@Composable
fun PermissionsHelpRoute(onBack: () -> Unit) {
    PermissionsHelpScreen(onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionsHelpScreen(onBack: () -> Unit) {
    val s = strings()
    val context = LocalContext.current

    // Pedido direto de notificações (Android 13+) com a mesma explicação da lista.
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> notifGranted = granted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = s.permissionHelpTitle) },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = s.actionClose,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(vertical = Spacing.sm),
        ) {
            if (!notifGranted) {
                item {
                    SectionHeader(title = s.permissionHelpNotifications)
                }
                item {
                    HelpSection(
                        body = s.permissionNotificationsRationale,
                    ) {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) {
                            Text(text = s.alarmsPermissionGrant)
                        }
                        TextButton(onClick = { openAppNotificationSettings(context) }) {
                            Text(text = s.permissionHelpOpenSettings)
                        }
                    }
                }
            }

            item {
                SectionHeader(title = s.permissionHelpExactAlarm)
            }
            item {
                HelpSection(body = s.permissionExactAlarmRationale)
            }

            item {
                SectionHeader(title = s.permissionHelpBattery)
            }
            item {
                HelpSection(
                    body = s.permissionBatteryHelp,
                ) {
                    TextButton(onClick = { openBatteryOptimizationSettings(context) }) {
                        Text(text = s.permissionHelpOpenSettings)
                    }
                }
            }
        }
    }
}

/** Cartão de ajuda simples: texto + ações opcionais. */
@Composable
private fun HelpSection(
    body: String,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.xs),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                actions()
            }
        }
    }
}

/** Abre as definições de notificações desta app (tela do sistema). */
private fun openAppNotificationSettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
        )
    }
}

/** Abre a lista de apps isentas da otimização de bateria. */
private fun openBatteryOptimizationSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }
}