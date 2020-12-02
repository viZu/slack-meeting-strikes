package xyz.ponsold.meetingstrikes

import com.slack.api.bolt.App
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import xyz.ponsold.meetingstrikes.action.*
import xyz.ponsold.meetingstrikes.action.ActionId.*
import xyz.ponsold.meetingstrikes.request.RequestContext

@Configuration
class Bootstrap(@Autowired val listStrikesAction: ListStrikesAction,
                @Autowired val addStrikeAction: AddStrikeAction,
                @Autowired val removeStrikeAction: RemoveStrikeAction,
                @Autowired val listReviewsAction: ListReviewsAction,
                @Autowired val approveStrikeProposalAction: ApproveStrikeProposalAction,
                @Autowired val declineStrikeProposalAction: DeclineStrikeProposalAction) {

    @Bean
    fun createApp(): App {
        val app = App()

        app.command("/strikes") { req, ctx ->
            if (req.payload.text == null) {
                return@command ctx.ack()
            }
            ctx.getLogger().info(req.payload.text)
            val paramText = req.payload.text.toLowerCase()
            val params = req.payload.text.split(" ".toRegex())
            val requestContext = RequestContext(ctx.teamId, ctx.channelId, ctx.requestUserId)

            when {
                paramText.startsWith("list") -> {
                    val response: ActionResponse = listStrikesAction.listStrikes(requestContext)
                    ctx.ack(response.toSlashCommandResponse())
                }
                paramText.startsWith("add") -> {
                    val count = if (params.size >= 3) params[2].toInt() else 1
                    val userid: String = params[1].parseUserId()
                    ctx.ack(addStrikeAction.addStrike(requestContext, userid, count).inChannel().toSlashCommandResponse())
                }
                paramText.startsWith("remove") -> {
                    val count = if (params.size >= 3) params[2].toInt() else 1
                    val userid: String = params[1].parseUserId()
                    ctx.ack(removeStrikeAction.removeStrike(requestContext, userid, count).inChannel().toSlashCommandResponse())
                }
                paramText.startsWith("reviews") -> {
                    val response: ActionResponse = listReviewsAction.listReviews(requestContext)
                    ctx.ack(response.toSlashCommandResponse())
                }
                paramText.startsWith("help") -> {
                    ctx.ack(helpText)
                }
                else -> ctx.ack(String.format("Unrecognized command '%s'\n%s", req.payload.text, helpText))
            }
        }

        app.blockAction(LIST_OVERFLOW_ACTIONS.pattern()) { req, ctx ->
            val requestContext = RequestContext(req.payload.team.id, req.payload.channel.id, req.payload.user.id)
            val actionId: String = req.payload.actions[0].selectedOption.value
            if (LIST_OVERFLOW_ACTIONS_ADD.matches(actionId)) {
                val response: ActionResponse = addStrikeAction.addStrike(requestContext, LIST_OVERFLOW_ACTIONS_ADD.getStringIdentifier(actionId), 1)
                ctx.respond(response.inChannel().toInteractiveResponse())
            } else if (LIST_OVERFLOW_ACTIONS_REMOVE.matches(actionId)) {
                val response: ActionResponse = removeStrikeAction.removeStrike(requestContext, LIST_OVERFLOW_ACTIONS_REMOVE.getStringIdentifier(actionId), 1)
                ctx.respond(response.inChannel().toInteractiveResponse())
            }
            ctx.ack()
        }

        app.blockAction(REVIEWS_APPROVE.pattern()) { req, ctx ->
            val selectedOption: String = req.payload.actions[0].actionId
            val requestContext = RequestContext(req.payload.team.id, req.payload.channel.id, req.payload.user.id)
            val id: Long = REVIEWS_APPROVE.getIdentifier(selectedOption)
            val approveResponse = approveStrikeProposalAction.approve(requestContext, id)
                    ?: listReviewsAction.listReviews(requestContext).replaceOriginal()
            ctx.respond(approveResponse.toInteractiveResponse())
            ctx.ack()
        }

        app.blockAction(NEW_STRIKE_APPROVED.pattern()) { req, ctx ->
            val selectedOption: String = req.payload.actions[0].actionId
            val requestContext = RequestContext(req.payload.team.id, req.payload.channel.id, req.payload.user.id)
            val id: Long = NEW_STRIKE_APPROVED.getIdentifier(selectedOption)
            val approveResponse: ActionResponse = approveStrikeProposalAction.approve(requestContext, id)
                    ?: ActionResponse.text("Got it :thumbsup:")
            ctx.respond(approveResponse.toInteractiveResponse())
            ctx.ack()
        }

        app.blockAction(REVIEWS_DECLINE.pattern()) { req, ctx ->
            val selectedOption: String = req.payload.actions[0].actionId
            val requestContext = RequestContext(req.payload.team.id, req.payload.channel.id, req.payload.user.id)
            val id: Long = REVIEWS_DECLINE.getIdentifier(selectedOption)
            val response: ActionResponse = declineStrikeProposalAction.decline(requestContext, id)
                    ?: listReviewsAction.listReviews(requestContext).replaceOriginal()
            ctx.respond(response.toInteractiveResponse())
            ctx.ack()
        }

        app.blockAction(NEW_STRIKE_DECLINED.pattern()) { req, ctx ->
            val selectedOption: String = req.payload.actions[0].actionId
            val requestContext = RequestContext(req.payload.team.id, req.payload.channel.id, req.payload.user.id)
            val id: Long = NEW_STRIKE_DECLINED.getIdentifier(selectedOption)
            val actionResponse: ActionResponse = declineStrikeProposalAction.decline(requestContext, id)
                    ?: ActionResponse.text("Got it :thumbsup:")
            ctx.respond(actionResponse.toInteractiveResponse())
            ctx.ack()
        }

        return app
    }

    companion object {
        val helpText = """
            ```
            Usage /strikes [command]
            
            Commands:
            list                   Lists all open strikes
            add [user] [count]     Creates a new strike proposal (count is optional)
            remove [user] [count]  Creates a new proposal for removing strikes (count is optional)
            reviews                Lists all pending reviews for strikes
            ```
            """.trimIndent()
    }
}