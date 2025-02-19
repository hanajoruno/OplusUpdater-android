package com.houvven.oplusupdater.ui.screen.home.components

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.houvven.oplusupdater.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.net.HttpURLConnection
import java.net.URL


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun UpdateLogDialog(
    show: Boolean,
    url: String,
    softwareVersion: String,
    onDismissRequest: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    var responseHtml by remember { mutableStateOf("") }

    LaunchedEffect(url) {
        launch(Dispatchers.IO) {
            Log.d("UpdateLogDialog", "startup get html")
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseHtml = conn.inputStream.bufferedReader().use { it.readText() }
            }
        }
    }

    if (!show) {
        return
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(if (isDarkTheme) Color(0xFF404040) else Color(0xFFFAFAFA)),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 26.dp)
                        .padding(top = 24.dp)
                        .zIndex(1f)
                ) {
                    Text(
                        text = stringResource(R.string.software_version),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W500,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = softwareVersion,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500,
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                    )
                    Box(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .height((0.5).dp)
                            .fillMaxWidth()
                            .background(MiuixTheme.colorScheme.dividerLine)
                    )
                }

                AndroidView(
                    factory = {
                        WebView(it).apply {
                            setBackgroundColor(Color.Transparent.toArgb())
                            // @formatter:off
                            addJavascriptInterface(object { @JavascriptInterface fun isNight(): Boolean = isDarkTheme }, "HeytapTheme")
                            // @formatter:on
                            settings.javaScriptEnabled = true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = {
                        it.loadDataWithBaseURL(url, responseHtml, "text/html", "utf-8", null)
                    }
                )
            }
        }
    }
}
