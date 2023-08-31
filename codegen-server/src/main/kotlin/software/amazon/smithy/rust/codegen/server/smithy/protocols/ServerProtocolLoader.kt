/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.rust.codegen.server.smithy.protocols

import software.amazon.smithy.aws.traits.protocols.AwsJson1_0Trait
import software.amazon.smithy.aws.traits.protocols.AwsJson1_1Trait
import software.amazon.smithy.aws.traits.protocols.RestJson1Trait
import software.amazon.smithy.aws.traits.protocols.RestXmlTrait
import software.amazon.smithy.rust.codegen.core.rustlang.Writable
import software.amazon.smithy.rust.codegen.core.rustlang.rust
import software.amazon.smithy.rust.codegen.core.rustlang.rustTemplate
import software.amazon.smithy.rust.codegen.core.rustlang.writable
import software.amazon.smithy.rust.codegen.core.smithy.RuntimeType
import software.amazon.smithy.rust.codegen.core.smithy.protocols.AwsJsonVersion
import software.amazon.smithy.rust.codegen.core.smithy.protocols.ProtocolLoader
import software.amazon.smithy.rust.codegen.core.smithy.protocols.ProtocolMap
import software.amazon.smithy.rust.codegen.server.smithy.ServerCodegenContext
import software.amazon.smithy.rust.codegen.server.smithy.generators.protocol.ServerProtocolGenerator

class StreamPayloadSerializerCustomization() : ServerHttpBoundProtocolCustomization() {
    override fun section(section: ServerHttpBoundProtocolSection): Writable = when (section) {
        is ServerHttpBoundProtocolSection.TypeOfSerializedStreamPayload -> writable {
            // `aws_smithy_http::byte_stream::ByteStream` no longer implements `futures_core::stream::Stream`
            // so wrap it in a new-type to enable the trait.
            rust(
                "#T",
                RuntimeType.hyperBodyWrapStream(section.params.runtimeConfig)
                    .resolve("HyperBodyWrapByteStream").toSymbol(),
            )
        }

        is ServerHttpBoundProtocolSection.WrapStreamPayload -> writable {
            rustTemplate(
                "#{HyperBodyWrapByteStream}::new(${section.params.payloadName!!})",
                "HyperBodyWrapByteStream" to RuntimeType.hyperBodyWrapStream(section.params.runtimeConfig)
                    .resolve("HyperBodyWrapByteStream"),
            )
        }

        else -> emptySection
    }
}

class ServerProtocolLoader(supportedProtocols: ProtocolMap<ServerProtocolGenerator, ServerCodegenContext>) :
    ProtocolLoader<ServerProtocolGenerator, ServerCodegenContext>(supportedProtocols) {

    companion object {
        val DefaultProtocols = mapOf(
            RestJson1Trait.ID to ServerRestJsonFactory(
                additionalServerHttpBoundProtocolCustomizations = listOf(
                    StreamPayloadSerializerCustomization(),
                ),
            ),
            RestXmlTrait.ID to ServerRestXmlFactory(
                additionalServerHttpBoundProtocolCustomizations = listOf(
                    StreamPayloadSerializerCustomization(),
                ),
            ),
            AwsJson1_0Trait.ID to ServerAwsJsonFactory(
                AwsJsonVersion.Json10,
                additionalServerHttpBoundProtocolCustomizations = listOf(StreamPayloadSerializerCustomization()),
            ),
            AwsJson1_1Trait.ID to ServerAwsJsonFactory(
                AwsJsonVersion.Json11,
                additionalServerHttpBoundProtocolCustomizations = listOf(StreamPayloadSerializerCustomization()),
            ),
        )
    }
}
