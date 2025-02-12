package com.houvven.oplusupdater.ui.screen.home.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.houvven.oplusupdater.R
import com.houvven.oplusupdater.domain.UpdateQueryResponse
import com.houvven.oplusupdater.utils.StorageUnitUtil
import com.houvven.oplusupdater.utils.toast
import kotlinx.serialization.json.Json
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.extra.RightActionColors
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperArrowDefaults
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SuperArrowWrapper(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    leftAction: @Composable (() -> Unit)? = null,
    rightText: String? = null,
    rightActionColor: RightActionColors = SuperArrowDefaults.rightActionColors(),
    onClick: (() -> Unit)? = null,
) {
    if (!summary.isNullOrBlank()) {
        SuperArrow(
            title = title,
            modifier = modifier,
            titleColor = titleColor,
            summary = summary,
            summaryColor = summaryColor,
            leftAction = leftAction,
            rightText = rightText,
            rightActionColor = rightActionColor,
            onClick = onClick,
        )
    }
}

@Composable
fun UpdateQueryResponseCard(
    responseBytes: ByteArray,
    modifier: Modifier = Modifier
) {
    val respStr = responseBytes.decodeToString()
    val json = Json { ignoreUnknownKeys = true }
    val resp = json.decodeFromString<UpdateQueryResponse>(respStr)
    UpdateQueryResponseCard(
        modifier = modifier,
        response = resp
    )
}

@Composable
fun UpdateQueryResponseCard(
    response: UpdateQueryResponse,
    modifier: Modifier = Modifier
) = with(response) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            SuperArrowWrapper(
                title = stringResource(R.string.version_type),
                summary = "$versionTypeH5 ($status)"
            )

            SuperArrowWrapper(
                title = stringResource(R.string.version_name),
                summary = versionName
            )
            SuperArrowWrapper(
                title = stringResource(R.string.android_version),
                summary = androidVersion ?: realAndroidVersion
            )
            SuperArrowWrapper(
                title = stringResource(R.string.os_version),
                summary = osVersion ?: realOsVersion ?: colorOSVersion
            )
            SuperArrowWrapper(
                title = stringResource(R.string.security_patch),
                summary = securityPatch ?: securityPatchVendor
            )
            SuperArrowWrapper(
                title = stringResource(R.string.published_time),
                summary = publishedTime?.let {
                    SimpleDateFormat("yyyy/MM/DD HH:mm:ss", Locale.getDefault())
                        .format(Date(it))
                }
            )
            SuperArrowWrapper(
                title = stringResource(R.string.update_log),
                summary = description?.panelUrl,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(description?.panelUrl))
                    context.startActivity(intent)
                }
            )
        }

        components?.forEach { component ->
            val componentPackets = component?.componentPackets ?: return@forEach
            Card {
                val size = componentPackets.size?.toLongOrNull()?.let(StorageUnitUtil::formatSize)

                SuperArrowWrapper(
                    title = stringResource(R.string.packet_name),
                    summary = component.componentName,
                    rightText = size
                )
                componentPackets.manualUrl?.let {
                    SuperArrowWrapper(
                        title = stringResource(R.string.packet_url),
                        summary = componentPackets.url,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(it))
                            context.toast(R.string.copied)
                        }
                    )
                }
                componentPackets.md5?.let {
                    SuperArrowWrapper(
                        title = stringResource(R.string.packet_md5),
                        summary = componentPackets.md5,
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