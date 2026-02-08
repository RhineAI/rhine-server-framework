package com.rhineai.framework.oss.aliyun

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.common.auth.DefaultCredentialProvider
import com.aliyun.oss.model.AbortMultipartUploadRequest
import com.aliyun.oss.model.CompleteMultipartUploadRequest
import com.aliyun.oss.model.InitiateMultipartUploadRequest
import com.aliyun.oss.model.PartETag
import com.aliyun.oss.model.UploadPartRequest
import com.rhineai.framework.constant.CommonErrorCode
import com.rhineai.framework.exception.BusinessException
import com.rhineai.framework.oss.OssService
import com.rhineai.framework.util.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.util.Date

@Service
class AliyunOssService(
    private val properties: AliyunOssProperties,
    private val tokenManager: AliyunOssTokenManager
) : OssService {
    private val logger = LoggerFactory.getLogger(AliyunOssService::class.java)

    override fun getToken(): Map<String, String> {
        return tokenManager.getToken()
    }

    override fun getFilePath(objectName: String): String {
        val token = getToken()
        val credentialsProvider =
            DefaultCredentialProvider(
                token["accessKey"].toString(),
                token["accessSecret"].toString(),
                token["token"].toString()
            )
        val ossClient = OSSClientBuilder().build(properties.endpoint, credentialsProvider)
        val expiration: Date = Date(Date().time + 3600 * 1000L)
        val url: URL = ossClient.generatePresignedUrl(properties.bucketName, objectName, expiration)
        return url.path
    }

    override fun uploadStream(inputStream: InputStream, objectName: String): String {
        val token = getToken()
        val credentialsProvider = DefaultCredentialProvider(
            token["accessKey"].toString(),
            token["accessSecret"].toString(),
            token["token"].toString()
        )

        val ossClient = OSSClientBuilder().build(properties.endpoint, credentialsProvider)
        return try {
            ossClient.putObject(properties.bucketName, objectName, inputStream)
            generateFileUrl(objectName)
        } finally {
            ossClient.shutdown()
            inputStream.close()
        }
    }

    private fun generateFileUrl(objectName: String): String {
        val ossClient = OSSClientBuilder().build(properties.endpoint, properties.accessKeyId, properties.accessKeySecret)
        val expiration = Date(Date().time + 3600 * 1000L)
        return ossClient.generatePresignedUrl(properties.bucketName, objectName, expiration).toString()
    }

    override fun uploadMultipartStream(fileContent: InputStream, preLabel: String, fileId: String, ext: String): String {
        return fileContent.use { stream ->
            val token = getToken()
            val credentialsProvider = DefaultCredentialProvider(
                token["accessKey"].toString(),
                token["accessSecret"].toString(),
                token["token"].toString()
            )
            val ossClient = OSSClientBuilder().build(properties.endpoint, credentialsProvider)
            val objectKey = "$preLabel${FileUtils().getFilePath(fileId, ext)}"
            val partSize = 5L * 1024 * 1024
            val partETags = mutableListOf<PartETag>()
            var uploadId: String? = null
            try {
                val initRequest = InitiateMultipartUploadRequest(properties.bucketName, objectKey)
                val initResult = ossClient.initiateMultipartUpload(initRequest)
                uploadId = initResult.uploadId
                val buffer = ByteArray(partSize.toInt())
                var partNumber = 1
                while (true) {
                    var bytesRead = 0
                    while (bytesRead < partSize) {
                        val read = stream.read(buffer, bytesRead, (partSize - bytesRead).toInt())
                        if (read == -1) break
                        bytesRead += read
                    }
                    if (bytesRead <= 0) {
                        break
                    }
                    val partStream = ByteArrayInputStream(buffer, 0, bytesRead)
                    val uploadPartRequest = UploadPartRequest()
                    uploadPartRequest.bucketName = properties.bucketName
                    uploadPartRequest.key = objectKey
                    uploadPartRequest.uploadId = uploadId
                    uploadPartRequest.partNumber = partNumber
                    uploadPartRequest.inputStream = partStream
                    uploadPartRequest.partSize = bytesRead.toLong()
                    val uploadPartResult = ossClient.uploadPart(uploadPartRequest)
                    partETags.add(uploadPartResult.partETag)
                    partNumber++
                    if (bytesRead < partSize) {
                        break
                    }
                }
                val completeRequest = CompleteMultipartUploadRequest(properties.bucketName, objectKey, uploadId, partETags)
                ossClient.completeMultipartUpload(completeRequest)
                generatePresignedUrl(objectKey, 3600L)
            } catch (e: Exception) {
                if (!uploadId.isNullOrBlank()) {
                    ossClient.abortMultipartUpload(AbortMultipartUploadRequest(properties.bucketName, objectKey, uploadId))
                }
                logger.error("Failed to upload file with fileId: $fileId, ext: $ext", e)
                throw BusinessException(CommonErrorCode.INTERNAL_ERROR, "File upload failed: ${e.message}")
            } finally {
                ossClient.shutdown()
            }
        }
    }

    override fun generatePresignedUrl(objectKey: String, expireSeconds: Long): String {
        val ossClient = OSSClientBuilder().build(properties.endpoint, properties.accessKeyId, properties.accessKeySecret)
        val expiration = Date(Date().time + expireSeconds * 1000L)
        return ossClient.generatePresignedUrl(properties.bucketName, objectKey, expiration).toString()
    }

    override fun deleteObject(objectKey: String): Boolean {
        val ossClient = OSSClientBuilder().build(properties.endpoint, properties.accessKeyId, properties.accessKeySecret)
        return try {
            ossClient.deleteObject(properties.bucketName, objectKey)
            logger.info("AliyunOssService.deleteObject: Successfully deleted object: $objectKey")
            true
        } catch (e: Exception) {
            logger.error("AliyunOssService.deleteObject: Failed to delete object: $objectKey", e)
            false
        } finally {
            ossClient.shutdown()
        }
    }
}
