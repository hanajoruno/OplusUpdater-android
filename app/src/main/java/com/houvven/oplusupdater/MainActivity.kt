package com.houvven.oplusupdater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

private val lightColor = lightColorScheme(
    primary = Color(0xFF5163FB),
    disabledPrimaryButton = Color(0xff8891da),
)

private val darkColor = lightColorScheme(
    primary = Color(0xff3544c6),
    disabledPrimaryButton = Color(0xff7276a3),
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiuixTheme(
                colors = if (isSystemInDarkTheme()) darkColor else lightColor
            ) {
                HomeScreen()
            }
        }
    }
}