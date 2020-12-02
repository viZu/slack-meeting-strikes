package xyz.ponsold.meetingstrikes.action

import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.PlainTextObject

fun String.markdown(): MarkdownTextObject {
    return MarkdownTextObject.builder()
            .text(this)
            .verbatim(false)
            .build()
}

fun String.plain(): PlainTextObject {
    return PlainTextObject.builder()
            .emoji(true)
            .text(this)
            .build()
}
