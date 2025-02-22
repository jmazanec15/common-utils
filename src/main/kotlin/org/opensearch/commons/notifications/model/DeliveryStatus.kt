/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.NotificationConstants.STATUS_CODE_TAG
import org.opensearch.commons.notifications.NotificationConstants.STATUS_TEXT_TAG
import org.opensearch.commons.utils.logger
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

/**
 * Data class representing Delivery Status.
 */
data class DeliveryStatus(
    val statusCode: String,
    val statusText: String
) : BaseModel {

    init {
        require(!Strings.isNullOrEmpty(statusCode)) { "StatusCode is null or empty" }
        require(!Strings.isNullOrEmpty(statusText)) { "statusText is null or empty" }
    }

    companion object {
        private val log by logger(DeliveryStatus::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { DeliveryStatus(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): DeliveryStatus {
            var statusCode: String? = null
            var statusText: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    STATUS_CODE_TAG -> statusCode = parser.text()
                    STATUS_TEXT_TAG -> statusText = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing deliveryStatus")
                    }
                }
            }
            statusCode ?: throw IllegalArgumentException("$STATUS_CODE_TAG field absent")
            statusText ?: throw IllegalArgumentException("$STATUS_TEXT_TAG field absent")
            return DeliveryStatus(
                statusCode,
                statusText
            )
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        statusCode = input.readString(),
        statusText = input.readString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(statusCode)
        output.writeString(statusText)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(STATUS_CODE_TAG, statusCode)
            .field(STATUS_TEXT_TAG, statusText)
            .endObject()
    }
}
