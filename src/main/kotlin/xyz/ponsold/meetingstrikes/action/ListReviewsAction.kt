package xyz.ponsold.meetingstrikes.action

import com.slack.api.model.block.*
import com.slack.api.model.block.composition.TextObject
import com.slack.api.model.block.element.ButtonElement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.dto.PendingApproval
import xyz.ponsold.meetingstrikes.entity.Modification
import xyz.ponsold.meetingstrikes.request.RequestContext
import xyz.ponsold.meetingstrikes.service.ProposalService
import xyz.ponsold.meetingstrikes.toEscapedUserId
import javax.transaction.Transactional

@Service
class ListReviewsAction(@Autowired val proposalService: ProposalService) {

    @Transactional
    fun listReviews(requestContext: RequestContext): ActionResponse {
        val pendingApprovals: List<PendingApproval> = proposalService.getPendingApprovals(requestContext)
        val blocks: MutableList<LayoutBlock> = pendingApprovals.flatMap { toLayoutBlocks(requestContext, it) }.toMutableList()

        if (blocks.isEmpty()) {
            return ActionResponse.text("There are no open reviews")
        }
        val header = HeaderBlock("reviews-header", "The following strikes are currently open for review".plain())

        blocks.add(0, header)
        return ActionResponse.blocks(blocks)
    }

    private fun toLayoutBlocks(requestContext: RequestContext, pendingApproval: PendingApproval): List<LayoutBlock> {
        val reviewText: TextObject = "${getCountText(pendingApproval)} ${pendingApproval.userId.toEscapedUserId()} ${getThumbsText(pendingApproval)}".markdown()
        val sectionBlock = SectionBlock(reviewText, "review-" + pendingApproval.id, null,
                null)
        val contextBlock = ContextBlock(listOf("Reported by ${pendingApproval.proposerUserId.toEscapedUserId()}".markdown()), "review-context-" + pendingApproval.id)
        return listOfNotNull(sectionBlock, contextBlock, getActionsBlock(requestContext, pendingApproval), DividerBlock())
    }

    private fun getCountText(pendingApproval: PendingApproval): String {
        return if (pendingApproval.modification == Modification.ADD) {
            "Add *${pendingApproval.strikeCount}* strikes for"
        } else {
            "Remove *${pendingApproval.strikeCount}* strikes for"
        }
    }

    private fun getThumbsText(pendingApproval: PendingApproval): String {
        return pendingApproval.approvedBy.joinToString { ":thumbsup:" } +
                pendingApproval.declinedBy.joinToString(separator = "") { ":thumbsdown:" }
    }

    private fun getActionsBlock(requestContext: RequestContext, pendingApproval: PendingApproval): ActionsBlock? {
        return if (shouldNotRenderActions(requestContext, pendingApproval)) {
            null
        } else ActionsBlock.builder()
                .blockId("review-actions-" + pendingApproval.id)
                .elements(listOf(
                        ButtonElement.builder()
                                .actionId(ActionId.REVIEWS_APPROVE.id(pendingApproval.id))
                                .value("review-approve-value-" + pendingApproval.id)
                                .style("primary")
                                .text("Approve".plain())
                                .build(),
                        ButtonElement.builder()
                                .actionId(ActionId.REVIEWS_DECLINE.id(pendingApproval.id))
                                .value("review-decline-value-" + pendingApproval.id)
                                .style("danger")
                                .text("Decline".plain())
                                .build()
                )).build()
    }

    private fun shouldNotRenderActions(requestContext: RequestContext, pendingApproval: PendingApproval): Boolean {
        val requester: String = requestContext.userId
        return (requester == pendingApproval.userId
                || requester == pendingApproval.proposerUserId
                || pendingApproval.approvedBy.any { ua -> ua.username == requester }
                || pendingApproval.declinedBy.any { ua -> ua.username == requester })
    }
}
