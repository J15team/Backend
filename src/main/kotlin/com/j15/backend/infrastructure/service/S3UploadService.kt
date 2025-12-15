package com.j15.backend.infrastructure.service

import com.j15.backend.infrastructure.config.S3Properties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.*

/**
 * S3画像アップロードサービス
 */
@Service
class S3UploadService(
        private val s3Properties: S3Properties
) {
    private val s3Client: S3Client = S3Client.builder()
            .region(Region.of(s3Properties.region))
            .build()

    companion object {
        private val ALLOWED_CONTENT_TYPES = setOf(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/webp"
        )
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    }

    /**
     * 画像をS3にアップロードし、URLを返す
     *
     * @param file アップロードする画像ファイル
     * @param folder 保存先フォルダ（オプション、デフォルトは"images"）
     * @return アップロードされた画像のURL
     * @throws IllegalArgumentException ファイルが無効な場合
     * @throws S3Exception S3アップロードに失敗した場合
     */
    fun uploadImage(file: MultipartFile, folder: String = "images"): String {
        // ファイル検証
        validateFile(file)

        // ファイル名生成（UUID + 元の拡張子）
        val originalFilename = file.originalFilename
                ?: throw IllegalArgumentException("ファイル名が取得できません")
        val extension = originalFilename.substringAfterLast('.', "")
        val fileName = "${UUID.randomUUID()}.${extension}"
        val key = "$folder/$fileName"

        try {
            // S3にアップロード
            val putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucketName)
                    .key(key)
                    .contentType(file.contentType)
                    .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.inputStream, file.size))

            // 公開URLを生成
            return "https://${s3Properties.bucketName}.s3.${s3Properties.region}.amazonaws.com/$key"
        } catch (e: S3Exception) {
            throw RuntimeException("S3への画像アップロードに失敗しました: ${e.message}", e)
        }
    }

    /**
     * ファイルの検証
     */
    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("ファイルが空です")
        }

        val contentType = file.contentType
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.lowercase())) {
            throw IllegalArgumentException("許可されていないファイル形式です。JPEG、PNG、GIF、WebPのみ許可されています。")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("ファイルサイズが大きすぎます。最大5MBまで許可されています。")
        }
    }
}

