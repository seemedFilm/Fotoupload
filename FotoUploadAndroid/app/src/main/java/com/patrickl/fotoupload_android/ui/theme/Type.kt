package com.patrickl.fotoupload_android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.patrickl.fotoupload_android.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
val notofont = FontFamily(
    Font(R.font.notoserif, FontWeight.Normal),
    Font(R.font.notoserif, FontWeight.Bold)
)
val LexendFont = FontFamily(
    Font(R.font.lexendgiga, FontWeight.Light),
    Font(R.font.lexendgiga, FontWeight.Normal),
    Font(R.font.lexendgiga, FontWeight.Medium),
    Font(R.font.lexendgiga, FontWeight.Bold)
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = LexendFont,
        fontSize = 50.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontFamily = LexendFont,
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontFamily = notofont,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontFamily = notofont,
        fontWeight = FontWeight.Bold
    )
)