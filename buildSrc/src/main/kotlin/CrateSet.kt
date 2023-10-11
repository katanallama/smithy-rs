/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

data class Crate(val name: String, val versionPropertyName: String)

object CrateSet {
    private const val DEV_PREVIEW_VERSION_PROP_NAME = "smithy.rs.runtime.crate.preview.version"
    const val STABLE_VERSION_PROP_NAME = "smithy.rs.runtime.crate.stable.version"

    val AWS_SDK_RUNTIME = listOf(
        Crate("aws-config", STABLE_VERSION_PROP_NAME),
        Crate("aws-credential-types", STABLE_VERSION_PROP_NAME),
        Crate("aws-endpoint", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-http", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-hyper", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-runtime", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-runtime-api", STABLE_VERSION_PROP_NAME),
        Crate("aws-sig-auth", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-sigv4", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-types", STABLE_VERSION_PROP_NAME),
    )

    private val SMITHY_RUNTIME_COMMON = listOf(
        Crate("aws-smithy-async", STABLE_VERSION_PROP_NAME),
        Crate("aws-smithy-checksums", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-client", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-eventstream", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-http", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-http-auth", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-http-tower", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-json", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-protocol-test", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-query", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-runtime", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-runtime-api", STABLE_VERSION_PROP_NAME),
        Crate("aws-smithy-types", STABLE_VERSION_PROP_NAME),
        Crate("aws-smithy-types-convert", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-xml", DEV_PREVIEW_VERSION_PROP_NAME),
    )

    val AWS_SDK_SMITHY_RUNTIME = SMITHY_RUNTIME_COMMON

    private val SERVER_SMITHY_RUNTIME = SMITHY_RUNTIME_COMMON + listOf(
        Crate("aws-smithy-http-server", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-http-server-python", DEV_PREVIEW_VERSION_PROP_NAME),
        Crate("aws-smithy-http-server-typescript", DEV_PREVIEW_VERSION_PROP_NAME),
    )

    val ENTIRE_SMITHY_RUNTIME = (AWS_SDK_SMITHY_RUNTIME + SERVER_SMITHY_RUNTIME).toSortedSet(compareBy { it.name })
}
