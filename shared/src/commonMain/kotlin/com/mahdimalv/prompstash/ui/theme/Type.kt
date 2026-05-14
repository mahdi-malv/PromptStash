package com.mahdimalv.prompstash.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mahdimalv.prompstash.resources.Res
import com.mahdimalv.prompstash.resources.STIXGeneralBold
import com.mahdimalv.prompstash.resources.STIXGeneralBoldItalic
import com.mahdimalv.prompstash.resources.STIXGeneralItalic
import com.mahdimalv.prompstash.resources.STIXGeneralRegular
import org.jetbrains.compose.resources.Font

@Composable
fun AppTypography(): Typography {
    val serif = FontFamily(
        Font(Res.font.STIXGeneralRegular, FontWeight.Normal),
        Font(Res.font.STIXGeneralItalic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.STIXGeneralBold, FontWeight.Bold),
        Font(Res.font.STIXGeneralBoldItalic, FontWeight.Bold, FontStyle.Italic),
    )
    val sans = FontFamily.SansSerif

    return Typography(
        displayLarge = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 40.sp,
            lineHeight = 48.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            lineHeight = 42.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 30.sp,
            lineHeight = 38.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 30.sp,
            lineHeight = 36.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 26.sp,
            lineHeight = 32.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 25.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        ),
    )
}
