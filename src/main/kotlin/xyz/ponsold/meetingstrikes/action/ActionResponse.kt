package xyz.ponsold.meetingstrikes.action

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.model.Attachment
import com.slack.api.model.block.LayoutBlock
import java.util.*

class ActionResponse {
    private var responseType = "ephemeral" // ephemeral, in_channel

    private var text: String? = null
    private var replaceOriginal = false
    private var deleteOriginal = false
    private val attachments: List<Attachment>? = null
    private var blocks: List<LayoutBlock>? = null

    fun inChannel(): ActionResponse {
        responseType = "in_channel"
        return this
    }

    fun replaceOriginal(): ActionResponse {
        replaceOriginal = true
        return this
    }

    fun deleteOriginal(): ActionResponse {
        deleteOriginal = true
        return this
    }

    fun toSlashCommandResponse(): SlashCommandResponse? {
        return SlashCommandResponse.builder()
                .responseType(responseType)
                .text(text)
                .blocks(blocks)
                .attachments(attachments)
                .build()
    }

    fun toInteractiveResponse(): com.slack.api.app_backend.interactive_components.response.ActionResponse {
        return com.slack.api.app_backend.interactive_components.response.ActionResponse.builder()
                .responseType(responseType)
                .text(text)
                .replaceOriginal(replaceOriginal)
                .deleteOriginal(deleteOriginal)
                .attachments(attachments)
                .blocks(blocks)
                .build()
    }

    companion object {
        fun text(text: String): ActionResponse {
            val actionResponse =  ActionResponse()
            actionResponse.text = text
            return actionResponse
        }

        fun blocks(vararg layoutBlocks: LayoutBlock): ActionResponse {
            return blocks(listOf(*layoutBlocks))
        }

        fun blocks(layoutBlocks: List<LayoutBlock>): ActionResponse {
            val actionResponse = ActionResponse()
            actionResponse.blocks = layoutBlocks
            return actionResponse
        }
    }
}