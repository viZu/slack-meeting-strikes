package xyz.ponsold.meetingstrikes.action

import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.element.ButtonElement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.entity.Strike
import xyz.ponsold.meetingstrikes.entity.StrikeProposal
import xyz.ponsold.meetingstrikes.request.RequestContext
import xyz.ponsold.meetingstrikes.service.ProposalService
import xyz.ponsold.meetingstrikes.service.StrikeService
import xyz.ponsold.meetingstrikes.toEscapedUserId
import java.util.*
import javax.transaction.Transactional

@Service
class AddStrikeAction(@Autowired val strikeService: StrikeService,
                      @Autowired val proposalService: ProposalService) {

    @Transactional
    fun addStrike(requestContext: RequestContext, userId: String, count: Int): ActionResponse {
        val strike: Strike = strikeService.createOrGetStrike(requestContext, userId)
        val proposal: StrikeProposal = proposalService.addToStrikes(requestContext, strike, count)

        val sectionBlock = SectionBlock.builder()
                .text("Proposal to add $count strikes for ${userId.toEscapedUserId()}".markdown())
                .blockId(UUID.randomUUID().toString())
                .build()

        val actions = ActionsBlock.builder().elements(listOf(
                ButtonElement.builder()
                        .actionId(ActionId.NEW_STRIKE_APPROVED.id(proposal.id))
                        .style("primary")
                        .text("Accept".plain())
                        .build(),
                ButtonElement.builder()
                        .actionId(ActionId.NEW_STRIKE_DECLINED.id(proposal.id))
                        .style("danger")
                        .text("Decline".plain())
                        .build()
        )).build()

        val contextBlock = ContextBlock.builder()
                .elements(listOf("created by ${requestContext.userId.toEscapedUserId()}".markdown()))
                .blockId(UUID.randomUUID().toString())
                .build()
        return ActionResponse.blocks(sectionBlock, actions, contextBlock)
    }
}