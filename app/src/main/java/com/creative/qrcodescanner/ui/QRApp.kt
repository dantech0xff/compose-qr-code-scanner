package com.creative.qrcodescanner.ui

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.withResumed
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.creative.qrcodescanner.R
import com.creative.qrcodescanner.ui.generate.GenerateQRScreenLayout
import com.creative.qrcodescanner.ui.history.HistoryScreenLayout
import com.creative.qrcodescanner.ui.main.MainScreenLayout
import com.creative.qrcodescanner.ui.main.MainViewModel
import com.creative.qrcodescanner.ui.main.QRCodeAction
import com.creative.qrcodescanner.ui.premium.PremiumScreenLayout
import com.creative.qrcodescanner.ui.result.QRCodeResultLayout
import com.creative.qrcodescanner.ui.setting.SettingScreenLayout
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun QRApp(vm: MainViewModel = hiltViewModel(),
          appNavHost: NavHostController = rememberNavController()) {

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(key1 = Unit) {
        vm.qrCodeActionState.collect {
            when (it) {
                is QRCodeAction.ToastAction -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is QRCodeAction.OpenUrl -> {
                    val url = it.url
                    lifecycle.withResumed {
                        if (url.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                }

                is QRCodeAction.OpenQRCodeResult -> {
                    if (it.id != MainViewModel.INVALID_DB_ROW_ID) {
                        appNavHost.navigate(
                            AppScreen.RESULT.value.replace(
                                "{id}",
                                it.id.toString()
                            )
                        )
                    }
                }

                is QRCodeAction.CopyText -> {
                    val copyText = it.text
                    if (copyText.isNotEmpty()) {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Copied Text", copyText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.resources.getString(R.string.copied_to_clipboard, copyText), Toast.LENGTH_SHORT).show()
                    }
                }

                is QRCodeAction.ContactInfo -> {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE
                        putExtra(ContactsContract.Intents.Insert.NAME, it.contact.name)
                        putExtra(ContactsContract.Intents.Insert.EMAIL, it.contact.email?.firstOrNull())
                        putExtra(ContactsContract.Intents.Insert.PHONE, it.contact.phone?.firstOrNull())
                        putExtra(ContactsContract.Intents.Insert.COMPANY, it.contact.organization)
                        putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it.contact.title)
                        putExtra(ContactsContract.Intents.Insert.NOTES, it.contact.urls?.firstOrNull())
                    }
                    context.startActivity(intent)
                }

                is QRCodeAction.TextSearchGoogle -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${it.text}"))
                    context.startActivity(intent)
                }

                is QRCodeAction.TextShareAction -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, it.text)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
                }

                is QRCodeAction.CallPhoneAction -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                    context.startActivity(intent)
                }

                is QRCodeAction.SendSMSAction -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${it.sms.number}"))
                    intent.putExtra("sms_body", it.sms.message)
                    context.startActivity(intent)
                }

                is QRCodeAction.PickGalleryImage -> {
                    val uri = it.uri
                    vm.showLoading()
                    BarcodeScanning.getClient().process(InputImage.fromFilePath(context, uri))
                        .addOnSuccessListener { barcodes ->
                            vm.hideLoading()
                            if (barcodes.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.no_qr_code_detected), Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            barcodes.forEach { barcode ->
                                if (barcode.rawValue != null) {
                                    vm.scanQRSuccess(barcode)
                                }
                            }
                        }
                        .addOnFailureListener {
                            vm.hideLoading()
                            Toast.makeText(context, context.getString(R.string.failed_to_scan_qr_code), Toast.LENGTH_SHORT).show()
                        }
                }

                else -> {}
            }
        }
    }

    NavHost(
        navController = appNavHost,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppScreen.MAIN.value
    ) {
        composable(route = AppScreen.MAIN.value) {
            MainScreenLayout(vm, appNavHost)
        }
        composable(route = AppScreen.RESULT.value) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getString("id")?.toInt() ?: 0
            QRCodeResultLayout(id, appNavHost, hiltViewModel(),
                {
                    vm.resetScanQR()
                }, { barCode ->
                    barCode?.let { vm.handleBarcodeResult(it) }
                }, {
                    // handle copy
                    vm.handleCopyText(it)
                }, {
                    // handle share
                    vm.handleShareText(it)
                })
        }
        composable(route = AppScreen.SETTING.value) {
            SettingScreenLayout(appNav = appNavHost)
        }
        composable(route = AppScreen.HISTORY.value) {
            HistoryScreenLayout(appNav = appNavHost)
        }
        composable(route = AppScreen.PREMIUM.value) {
            PremiumScreenLayout(appNav = appNavHost)
        }
        composable(route = AppScreen.GENERATE.value) {
            GenerateQRScreenLayout(appNav = appNavHost)
        }
    }
}

enum class AppScreen(val value: String) {
    MAIN("main"),
    SETTING("setting"),
    PREMIUM("premium"),
    HISTORY("history"),
    RESULT("result/{id}"),
    GENERATE("generate")
}