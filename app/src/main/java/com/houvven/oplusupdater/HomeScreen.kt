package com.houvven.oplusupdater

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.Updater.queryUpdaterRawBytes

private const val Unknown = "Unknown"

enum class OtaPacketStatus {
    Published,
    Testing
}

enum class OtaZone(
    @StringRes val strRes: Int,
) {
    CN(R.string.china),
    EU(R.string.europe),
    IN(R.string.india),
    SG(R.string.singapore)
}

@delegate:SuppressLint("PrivateApi")
val systemOtaVersion: String by lazy {
    val clazz = Class.forName("android.os.SystemProperties")
    val method = clazz.getMethod("get", String::class.java, String::class.java)
    method.invoke(clazz, "ro.build.version.ota", "") as String
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    val scrollState = rememberScrollState()

    var otaVersion by rememberSaveable { mutableStateOf(systemOtaVersion) }
    var otaZone by rememberSaveable { mutableStateOf(OtaZone.CN) }
    var otaMode by rememberSaveable { mutableStateOf(OtaPacketStatus.Published) }
    var response by rememberSaveable { mutableStateOf<ByteArray?>(null) }
    val errMsgFlow = MutableSharedFlow<String>()

    LaunchedEffect(errMsgFlow) {
        errMsgFlow.collectLatest {
            withContext(Dispatchers.Main) {
                if (it.contains("code: 304")) {
                    context.toast(R.string.msg_no_update_available)
                } else {
                    context.toast(it)
                }
            }
        }
    }


    Scaffold(
        topBar = { SmallTopAppBar(title = "Updater") },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = otaVersion,
                onValueChange = { otaVersion = it.trim() },
                label = stringResource(R.string.ota_version),
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.surface)
            ) {
                SuperDropdown(
                    title = stringResource(R.string.region),
                    items = OtaZone.entries.map { it.name },
                    selectedIndex = OtaZone.entries.indexOf(otaZone)
                ) {
                    otaZone = OtaZone.entries[it]
                }

                SuperDropdown(
                    modifier = Modifier,
                    title = stringResource(R.string.mode),
                    items = OtaPacketStatus.entries.map { it.name },
                    selectedIndex = OtaPacketStatus.entries.indexOf(otaMode)
                ) {
                    otaMode = OtaPacketStatus.entries[it]
                }
            }

            Button(
                onClick = {
                    response = null
                    val attr = updater.Attribute().also {
                        it.otaVer = otaVersion
                        it.zone = otaZone.name.lowercase()
                        it.mode = otaMode.ordinal.toLong()
                    }
                    try {
                        response = queryUpdaterRawBytes(attr).takeIf { it.isNotEmpty() }
                        coroutineScope.launch {
                            errMsgFlow.emit(context.getString(R.string.msg_query_success))
                        }
                    } catch (e: Exception) {
                        coroutineScope.launch {
                            errMsgFlow.emit(e.message ?: e.stackTraceToString())
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                enabled = otaVersion.isNotBlank(),
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(
                    text = stringResource(R.string.query),
                    color = MiuixTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            AnimatedVisibility(visible = response != null) {
                UpdaterQueryResponseCard(response!!)
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
private fun UpdaterQueryResponseCard(
    responseBytes: ByteArray
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val json = Json { ignoreUnknownKeys = true }
    val response = json.decodeFromStream<UpdaterQueryResponse>(responseBytes.inputStream())

    if (response.components == null) {
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            SuperArrow(
                title = stringResource(R.string.status),
                summary = response.status
            )
            response.description.panelUrl.let {
                SuperArrow(
                    title = stringResource(R.string.updatelog),
                    summary = response.description.panelUrl,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        context.startActivity(intent)
                    }
                )
            }
            response.realAndroidVersion?.let {
                SuperArrow(
                    title = stringResource(R.string.android_version),
                    summary = it.ifBlank { Unknown }
                )
            }
            response.realOsVersion?.let {
                SuperArrow(
                    title = stringResource(R.string.os_version),
                    summary = it.ifBlank { Unknown }
                )
            }
            response.realOtaVersion?.let {
                SuperArrow(
                    title = stringResource(R.string.ota_version),
                    summary = it.ifBlank { Unknown }
                )
            }
            response.securityPatch?.let {
                SuperArrow(
                    title = stringResource(R.string.security_patch),
                    summary = it.ifBlank { Unknown }
                )
            }
        }

        response.components.forEach { component ->
            val componentPackets = component?.componentPackets
            if (componentPackets != null) {
                val size = componentPackets.size.toDoubleOrNull()
                    ?.div(1024 * 1024 * 1024)
                    ?.let { "%.3f".format(it) + " GB" }

                Card {
                    SuperArrow(
                        title = stringResource(R.string.packet_name),
                        summary = component.componentName
                    )
                    SuperArrow(
                        title = stringResource(R.string.packet_size),
                        summary = size
                    )
                    componentPackets.manualUrl.let {
                        SuperArrow(
                            title = stringResource(R.string.packet_url),
                            summary = it,
                            onClick = {
                                clipboardManager.setText(AnnotatedString(it))
                                context.toast(R.string.copied)
                            }
                        )
                    }
                    componentPackets.md5.let {
                        SuperArrow(
                            title = stringResource(R.string.packet_md5),
                            summary = it,
                            onClick = {
                                clipboardManager.setText(AnnotatedString(it))
                                context.toast(R.string.copied)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    HomeScreen()
}