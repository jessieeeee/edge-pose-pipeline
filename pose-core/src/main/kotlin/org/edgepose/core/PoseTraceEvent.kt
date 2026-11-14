package org.edgepose.core

data class PoseTraceEvent(
    val sessionId: String,
    val sequence: Long,
    val frameId: Long,
    val monotonicTimestampNanos: Long,
    val type: String,
    val fields: Map<String, Any?> = emptyMap(),
) {
    init {
        require(sessionId.isNotBlank())
        require(sequence >= 0L && frameId >= 0L && monotonicTimestampNanos >= 0L)
        require(type.isNotBlank())
    }
}

object PoseTraceJson {
    fun encode(event: PoseTraceEvent): String = buildString {
        append('{')
        append("\"schemaVersion\":1,")
        append("\"sessionId\":").appendString(event.sessionId).append(',')
        append("\"sequence\":").append(event.sequence).append(',')
        append("\"frameId\":").append(event.frameId).append(',')
        append("\"monotonicTimestampNanos\":").append(event.monotonicTimestampNanos).append(',')
        append("\"type\":").appendString(event.type)
        event.fields.toSortedMap().forEach { (name, value) ->
            append(',').appendString(name).append(':')
            appendValue(value)
        }
        append("}\n")
    }

    private fun StringBuilder.appendValue(value: Any?) {
        when (value) {
            null -> append("null")
            is String -> appendString(value)
            is Boolean, is Byte, is Short, is Int, is Long -> append(value)
            is Float -> if (value.isFinite()) append(value) else append("null")
            is Double -> if (value.isFinite()) append(value) else append("null")
            is Map<*, *> -> {
                append('{')
                value.entries.filter { it.key is String }.sortedBy { it.key as String }.forEachIndexed { index, entry ->
                    if (index > 0) append(',')
                    appendString(entry.key as String).append(':')
                    appendValue(entry.value)
                }
                append('}')
            }
            is Iterable<*> -> {
                append('[')
                value.forEachIndexed { index, item ->
                    if (index > 0) append(',')
                    appendValue(item)
                }
                append(']')
            }
            else -> error("Unsupported trace field type: ${value::class.qualifiedName}")
        }
    }

    private fun StringBuilder.appendString(value: String): StringBuilder {
        append('"')
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000c' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) append("\\u%04x".format(character.code)) else append(character)
            }
        }
        return append('"')
    }
}
