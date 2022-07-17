package xyz.wingio.logra.ui.components.permission

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import xyz.wingio.logra.utils.grantPermissionsWithShizuku

@Composable
fun GrantAndClosePopup(onDialogChange: (PopupState) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        text = {
            Text(
                text = "Once granted, the app will restart. Press the button below to grant the permission and restart the app.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        grantPermissionsWithShizuku()
                    }
                },
                content = {
                    Text(
                        text = "Grant and close"
                    )
                }
            )
        },
        onDismissRequest = {
            onDialogChange(PopupState.NONE)
        }
    )
}

enum class GrantMethod {
    ROOT,
    SHIZUKU
}