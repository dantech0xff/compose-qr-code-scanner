package com.creative.qrcodescanner.ui.main

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.creative.qrcodescanner.R
import com.creative.qrcodescanner.ui.AppScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.random.Random

/**
 * Created by dan on 07/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */
 
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreenLayout(vm: MainViewModel, appNavHost: NavHostController) {

    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }.apply {
        bindToLifecycle(LocalLifecycleOwner.current)
    }

    val galleryPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), onResult = {
        vm.handleGalleryUri(it)
    })

    var appSettingState by remember {
        mutableStateOf(vm.appSettingState.value)
    }

    var mainUIState by remember {
        mutableStateOf(vm.mainUiState.value)
    }

    val isLoadingState by vm.mainUiState.map { it.isLoading }.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(key1 = Unit) {
        vm.appSettingState.collectLatest {
            appSettingState = it
        }
    }

    LaunchedEffect(key1 = Unit) {
        vm.mainUiState.collectLatest {
            mainUIState = it

            cameraController.cameraSelector = if (it.isFrontCamera && cameraController.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            cameraController.enableTorch(it.isTorchOn)

            if (it.isQRCodeFound) {
                if (appSettingState?.isEnableVibrate == true) {
                    context.vibrate(200L)
                }
                if (appSettingState?.isEnableSound == true) {
                    context.playPiplingSound()
                }
                cameraController.clearImageAnalysisAnalyzer()
            } else {
                delay(Random(Calendar.getInstance().timeInMillis).nextLong(1000))
                cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    imageProxy.image?.let { img ->
                        InputImage.fromMediaImage(img, imageProxy.imageInfo.rotationDegrees)
                            .let { image ->
                                val scanner = BarcodeScanning.getClient()
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isEmpty()) {
                                            return@addOnSuccessListener
                                        }
                                        barcodes.forEach { barcode ->
                                            if (barcode.rawValue != null) {
                                                vm.scanQRSuccess(barcode)
                                            }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        exception.printStackTrace()
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (cameraPermissionState.status.isGranted) {
                QRCameraView(cameraController,
                    appSettingState,
                    handleSwitchKeepScanning = {
                        vm.toggleKeepScanning()
                    })
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(R.string.camera_permission_is_not_granted), color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text(text = stringResource(R.string.click_to_grant_camera_permission), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Box(
            Modifier
                .safeDrawingPadding()
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TopTools(
                mainUIState,
                appSettingState,
                toggleTorch = remember {
                    vm::toggleTorch
                },
                toggleCamera = remember {
                    vm::toggleCamera
                },
                navSetting = remember {
                    {
                        appNavHost.navigate(AppScreen.SETTING.value)
                    }
                },
                navPremium = remember {
                    {
                        appNavHost.navigate(AppScreen.PREMIUM.value)
                    }
                }
            )

            FooterTools(
                navToHistory = remember {
                    {
                        appNavHost.navigate(AppScreen.HISTORY.value)
                    }
                },
                pickGallery = remember {
                    {
                        galleryPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                },
                navToGenerate = remember {
                    {
                        appNavHost.navigate(AppScreen.GENERATE.value)
                    }
                })
        }

        AnimatedVisibility(visible = isLoadingState, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(0x901c1c1c))
                .clickable { }) {
                Text(
                    text = stringResource(R.string.loading_qr_code_scanner_engine),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }
}

fun Context.vibrate(milliseconds: Long) {
    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        v?.vibrate(milliseconds)
    }
}

fun Context.playPiplingSound() {
    val mediaPlayer = MediaPlayer.create(this, R.raw.ping).apply {
        setOnCompletionListener {
            it.release()
        }
        setVolume(0.5f, 0.5f)
    }
    mediaPlayer.start()
}