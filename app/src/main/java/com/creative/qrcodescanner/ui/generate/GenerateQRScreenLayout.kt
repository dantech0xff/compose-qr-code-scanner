package com.creative.qrcodescanner.ui.generate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.creative.qrcodescanner.R
import com.creative.qrcodescanner.ui.nav.TopNavBar
import com.creative.qrcodescanner.ui.shadow

@Composable
fun GenerateQRScreenLayout(
    appNav: NavHostController,
    viewModel: GenerateQRViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler {
        appNav.popBackStack()
    }

    Scaffold(
        topBar = {
            TopNavBar(titleResId = R.string.generate_qr_code) {
                appNav.popBackStack()
            }
        },
        bottomBar = {
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text(text = stringResource(R.string.enter_text_to_generate_qr)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    viewModel.generateQRCode(inputText)
                })
            )

            TextButton(
                onClick = {
                    keyboardController?.hide()
                    viewModel.generateQRCode(inputText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        blurRadius = 8.dp, borderRadius = 8.dp, spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                    )
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = stringResource(R.string.generate).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            when (val state = uiState) {
                is GenerateQRUIState.Success -> {
                    Image(
                        bitmap = state.bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.qr_code_generated),
                        modifier = Modifier
                            .size(256.dp)
                            .shadow(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                blurRadius = 8.dp, borderRadius = 8.dp, spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                            )
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }

                is GenerateQRUIState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is GenerateQRUIState.Idle -> {
                    // No content to show yet
                }
            }
        }
    }
}
