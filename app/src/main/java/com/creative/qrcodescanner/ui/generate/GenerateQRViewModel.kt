package com.creative.qrcodescanner.ui.generate

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class GenerateQRUIState {
    data object Idle : GenerateQRUIState()
    data class Success(val bitmap: Bitmap) : GenerateQRUIState()
    data class Error(val message: String) : GenerateQRUIState()
}

@HiltViewModel
class GenerateQRViewModel @Inject constructor() : ViewModel() {

    private val _uiState: MutableStateFlow<GenerateQRUIState> = MutableStateFlow(GenerateQRUIState.Idle)
    val uiState: StateFlow<GenerateQRUIState> = _uiState.asStateFlow()

    fun generateQRCode(text: String, size: Int = 512) {
        if (text.isBlank()) {
            _uiState.value = GenerateQRUIState.Error("Please enter text")
            return
        }
        try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val pixels = IntArray(size * size)
            for (y in 0 until size) {
                for (x in 0 until size) {
                    pixels[y * size + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            _uiState.value = GenerateQRUIState.Success(bitmap)
        } catch (e: Exception) {
            _uiState.value = GenerateQRUIState.Error(e.message ?: "Failed to generate QR code")
        }
    }
}
