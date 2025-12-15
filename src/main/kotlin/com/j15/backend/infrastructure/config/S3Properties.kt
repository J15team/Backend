package com.j15.backend.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * AWS S3の設定プロパティ
 *
 * application.ymlのaws.s3設定を読み込む
 */
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
data class S3Properties(
        /** S3バケット名 */
        var bucketName: String = "",
        /** AWSリージョン */
        var region: String = "ap-northeast-1"
)

