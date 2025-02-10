package com.houvven.oplusupdater


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdaterQueryResponse(
    @SerialName("components")
    val components: List<Component?>? = null,
    @SerialName("description")
    val description: Description,
    @SerialName("realAndroidVersion")
    val realAndroidVersion: String? = null,
    @SerialName("realOsVersion")
    val realOsVersion: String? = null,
    @SerialName("realOtaVersion")
    val realOtaVersion: String? = null,
    @SerialName("realVersionName")
    val realVersionName: String? = null,
    @SerialName("securityPatch")
    val securityPatch: String? = null,
    @SerialName("securityPatchVendor")
    val securityPatchVendor: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("versionTypeH5")
    val versionTypeH5: String? = null
) {
    @Serializable
    data class Component(
        @SerialName("componentId")
        val componentId: String,
        @SerialName("componentName")
        val componentName: String,
        @SerialName("componentPackets")
        val componentPackets: ComponentPackets,
        @SerialName("componentVersion")
        val componentVersion: String
    ) {
        @Serializable
        data class ComponentPackets(
            @SerialName("id")
            val id: String,
            @SerialName("manualUrl")
            val manualUrl: String,
            @SerialName("md5")
            val md5: String,
            @SerialName("size")
            val size: String,
            @SerialName("url")
            val url: String
        )
    }

    @Serializable
    data class Description(
        @SerialName("firstTitle")
        val firstTitle: String? = null,
        @SerialName("panelUrl")
        val panelUrl: String? = null,
        @SerialName("url")
        val url: String? = null
    )
}