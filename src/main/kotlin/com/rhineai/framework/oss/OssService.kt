package com.rhineai.framework.oss

import java.io.InputStream

interface OssService {
    fun getToken(): Map<String, String>
    fun getFilePath(objectName: String): String
    fun uploadStream(inputStream: InputStream, objectName: String): String
    fun uploadMultipartStream(fileContent: InputStream, preLabel: String, fileId: String, ext: String): String
    fun generatePresignedUrl(objectKey: String, expireSeconds: Long = 3600L): String
    fun deleteObject(objectKey: String): Boolean
}
