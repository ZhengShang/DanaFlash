package com.ecreditpal.danaflash.model

data class OssStsRes(
    val assumedRoleUser: AssumedRoleUser?,
    val credentials: Credentials?,
    val requestId: String? // 1234
) {
    data class AssumedRoleUser(
        val arn: String?, // aa
        val assumedRoleId: String? // test
    )

    data class Credentials(
        val accessKeyId: String?, // 1234
        val accessKeySecret: String?, // sec
        val expiration: String?, // 2020
        val securityToken: String? // token
    )
}