package com.rhineai.framework.util

import com.rhineai.framework.oss.aliyun.AliyunOssService
import com.rhineai.framework.oss.aliyun.AliyunOssTokenManager
import java.io.InputStream

class AliOSSUtils(
    private val tokenManager: AliyunOssTokenManager,
    private val aliyunOssService: AliyunOssService
) {
    fun getAuthToken() {
        tokenManager.getAuthToken()
    }

    fun getToken(): Map<String, String> {
        return tokenManager.getToken()
    }

    fun getFilePath(objectName: String): String {
        return aliyunOssService.getFilePath(objectName)
    }

    fun uploadStream(inputStream: InputStream, objectName: String): String {
        return aliyunOssService.uploadStream(inputStream, objectName)
    }

    fun uploadMultipartStream(fileContent: InputStream, preLabel: String, fileId: String, ext: String): String {
        return aliyunOssService.uploadMultipartStream(fileContent, preLabel, fileId, ext)
    }

    fun generatePresignedUrl(objectKey: String, expireSeconds: Long): String {
        return aliyunOssService.generatePresignedUrl(objectKey, expireSeconds)
    }

    fun deleteObject(objectKey: String): Boolean {
        return aliyunOssService.deleteObject(objectKey)
    }
}
