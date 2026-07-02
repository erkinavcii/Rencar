package com.turkcell.rencar_pair.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Replace FontFamily.Default with bundled TTF files.
// Add Sora and Plus Jakarta Sans TTF files to res/font/ from https://fonts.google.com,
// then define FontFamily(...) with Font(R.font.sora_bold, ...) entries.
val SoraFamily: FontFamily = FontFamily.Default
val PlusJakartaSansFamily: FontFamily = FontFamily.Default

// Display — Sora
val displayXL = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, letterSpacing = (-1.0).sp)
val displayL  = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, letterSpacing = (-1.5).sp)
val displayM  = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-1.0).sp)
val displayS  = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.Bold,      fontSize = 27.sp, letterSpacing = (-0.6).sp)

// Heading — Sora
val headingXL = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.5).sp)
val headingL  = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.Bold,      fontSize = 21.sp, letterSpacing = (-0.4).sp)
val headingM  = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.Bold,      fontSize = 20.sp, letterSpacing = 0.sp)
val headingS  = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.Bold,      fontSize = 19.sp, letterSpacing = 0.sp)
val headingXS = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.Bold,      fontSize = 18.sp, letterSpacing = 0.sp)

// Price / Stat — Sora
val priceL    = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.5).sp)
val priceM    = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 0.sp)
val statValue = TextStyle(fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = 0.sp)

// Title — Plus Jakarta Sans
val titleL  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 16.5.sp, letterSpacing = 0.sp)
val titleM  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, letterSpacing = 0.sp)
val titleS  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp,   letterSpacing = 0.sp)
val titleXS = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, letterSpacing = 0.sp)

// Body — Plus Jakarta Sans
val bodyL  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp,   lineHeight = (15 * 1.55).sp,  letterSpacing = 0.sp)
val bodyM  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = (13.5 * 1.5).sp, letterSpacing = 0.sp)
val bodyS  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp,   lineHeight = (13 * 1.5).sp,   letterSpacing = 0.sp)
val bodyXS = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, lineHeight = (12.5 * 1.4).sp, letterSpacing = 0.sp)

// Label / Caption — Plus Jakarta Sans
val labelM     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 12.sp,   letterSpacing = 0.sp)
val labelS     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 11.5.sp, letterSpacing = 0.sp)
val labelXS    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,   letterSpacing = 0.sp)
val labelMicro = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 10.5.sp, letterSpacing = 0.sp)

// Material3 Typography slot mapping
val RencarTypography = Typography(
    displayLarge   = displayXL,
    displayMedium  = displayL,
    displaySmall   = displayM,
    headlineLarge  = headingXL,
    headlineMedium = headingL,
    headlineSmall  = headingM,
    titleLarge     = titleL,
    titleMedium    = titleM,
    titleSmall     = titleS,
    bodyLarge      = bodyL,
    bodyMedium     = bodyM,
    bodySmall      = bodyS,
    labelLarge     = labelM,
    labelMedium    = labelS,
    labelSmall     = labelXS,
)
