package com.houvven.oplusupdater.ui.screen.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.houvven.oplusupdater.R
import com.houvven.oplusupdater.ui.screen.home.components.UpdateQueryResponseCard
import com.houvven.oplusupdater.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.GitHub
import top.yukonga.miuix.kmp.icon.icons.Info
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.Updater.queryUpdaterRawBytes


enum class OtaPacketStatus {
    Published,
    Testing
}

@Keep
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

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
    val scrollState = rememberScrollState()

    var otaVersion by rememberSaveable { mutableStateOf(systemOtaVersion) }
    var otaZone by rememberSaveable { mutableStateOf(OtaZone.CN) }
    var otaMode by rememberSaveable { mutableStateOf(OtaPacketStatus.Published) }
    var proxy by rememberSaveable { mutableStateOf("") }
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
        topBar = {
            SmallTopAppBar(
                title = "Updater",
                actions = {
                    IconButton(onClick = {
                        val repoUrl = "https://github.com/houvven/OplusUpdater-android"
                        Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl)).let(context::startActivity)
                    }) {
                        Icon(
                            imageVector = MiuixIcons.GitHub,
                            contentDescription = null
                        )
                    }
                }
            )
        },
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
            TextField(
                value = proxy,
                onValueChange = { proxy = it.trim() },
                label = stringResource(R.string.proxy)
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.surface)
            ) {
                SuperDropdown(
                    title = stringResource(R.string.region),
                    items = OtaZone.entries.map { stringResource(it.strRes) },
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
                        it.zone = otaZone.name
                        it.mode = otaMode.ordinal.toLong()
                        it.proxyStr = proxy
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
                response?.let { UpdateQueryResponseCard(it) }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    HomeScreen()
}