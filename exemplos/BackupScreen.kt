package com.dailycompany.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailycompany.data.backup.ImportStrategy
import com.dailycompany.ui.theme.strings

/**
 * Ecrã "Dados": exporta tudo para um .zip escolhido pelo utilizador e volta a importá-lo.
 * O picker é do sistema (SAF), por isso não é preciso nenhuma permissão de armazenamento.
 */
@Composable
fun BackupRoute(viewModel: BackupViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val s = strings()
    val state by viewModel.state.collectAsState()
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var confirmReplace by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> pendingUri = uri }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(s.backupTitle, style = MaterialTheme.typography.titleLarge)

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.backupExport, style = MaterialTheme.typography.titleMedium)
                Text(s.backupExportDescription, style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = { exportLauncher.launch(viewModel.suggestedFileName()) },
                    enabled = !state.busy,
                ) { Text(s.backupExport) }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.backupImport, style = MaterialTheme.typography.titleMedium)
                Text(s.backupImportDescription, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/zip")) },
                    enabled = !state.busy,
                ) { Text(s.backupImport) }
            }
        }

        if (state.busy) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(s.backupInProgress)
            }
        }
        state.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }

    // Escolha da estratégia
    pendingUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingUri = null },
            title = { Text(s.backupStrategyTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${s.backupStrategyMerge} — ${s.backupStrategyMergeDescription}")
                    Text("${s.backupStrategyReplace} — ${s.backupStrategyReplaceDescription}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.import(uri, ImportStrategy.MERGE); pendingUri = null
                }) { Text(s.backupStrategyMerge) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReplace = true }) { Text(s.backupStrategyReplace) }
            },
        )
    }

    // Confirmação extra para "substituir tudo"
    if (confirmReplace) {
        val uri = pendingUri
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text(s.backupStrategyReplace) },
            text = { Text(s.backupReplaceConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    uri?.let { viewModel.import(it, ImportStrategy.REPLACE) }
                    confirmReplace = false; pendingUri = null
                }) { Text(s.actionConfirm) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReplace = false }) { Text(s.actionCancel) }
            },
        )
    }
}
