package com.creative.qrcodescanner.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.creative.qrcodescanner.R
import com.creative.qrcodescanner.ui.shadow
import com.creative.qrcodescanner.ui.theme.QRCodeScannerTheme

@Composable
@Stable
fun BoxScope.FooterTools(navToHistory: () -> Unit, pickGallery: () -> Unit, navToGenerate: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.add_photo_alternate),
        contentDescription = stringResource(R.string.gallery_picker),
        contentScale = ContentScale.Inside,
        modifier = Modifier
            .padding(16.dp)
            .shadow(Color(0x901c1c1c).copy(alpha = 0.5f), blurRadius = 12.dp, borderRadius = 32.dp, spread = 0.dp, offsetY = 1.dp, offsetX = 1.dp)
            .align(Alignment.BottomStart)
            .size(64.dp)
            .background(color = Color(0x901c1c1c), shape = CircleShape)
            .clip(CircleShape)
            .clickable(onClick = pickGallery)
            .padding(18.dp)
    )

    Image(
        painter = painterResource(id = R.drawable.icon_qr_generate),
        contentDescription = stringResource(R.string.generate_qr_button),
        contentScale = ContentScale.Inside,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
            .shadow(Color(0x901c1c1c).copy(alpha = 0.5f), blurRadius = 12.dp, borderRadius = 32.dp, spread = 0.dp, offsetY = 1.dp, offsetX = 1.dp)
            .size(64.dp)
            .background(color = Color(0x901c1c1c), shape = CircleShape)
            .clip(CircleShape)
            .clickable(onClick = navToGenerate)
            .padding(18.dp)
    )

    Image(
        painter = painterResource(id = R.drawable.history),
        contentDescription = stringResource(R.string.history_picker),
        contentScale = ContentScale.Inside,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .shadow(Color(0x901c1c1c).copy(alpha = 0.5f), blurRadius = 12.dp, borderRadius = 32.dp, spread = 0.dp, offsetY = 1.dp, offsetX = 1.dp)
            .size(64.dp)
            .background(color = Color(0x901c1c1c), shape = CircleShape)
            .clip(CircleShape)
            .clickable(onClick = navToHistory)
            .padding(18.dp)
    )
}

@Preview
@Composable
fun FooterToolsPreview() {
    QRCodeScannerTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            FooterTools(navToHistory = {}, pickGallery = {}, navToGenerate = {})
        }
    }
}