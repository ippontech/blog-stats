package tech.ippon.blog.stats.model

import com.google.gson.annotations.SerializedName

data class GitFile(
        val name: String,
        @SerializedName("download_url") val downloadUrl: String)