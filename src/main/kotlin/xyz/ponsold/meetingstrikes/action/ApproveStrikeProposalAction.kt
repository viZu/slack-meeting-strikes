package xyz.ponsold.meetingstrikes.action

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.entity.Modification
import xyz.ponsold.meetingstrikes.entity.Strike
import xyz.ponsold.meetingstrikes.entity.StrikeProposal
import xyz.ponsold.meetingstrikes.request.RequestContext
import xyz.ponsold.meetingstrikes.service.ProposalAlreadyClosedException
import xyz.ponsold.meetingstrikes.service.ProposalService
import xyz.ponsold.meetingstrikes.service.ProposerOfProposalException
import xyz.ponsold.meetingstrikes.service.StrikeService
import javax.transaction.Transactional

@Service
class ApproveStrikeProposalAction(
        @Autowired val proposalService: ProposalService,
        @Autowired val strikeService: StrikeService
) {

    @Transactional
    fun approve(requestContext: RequestContext, proposalId: Long): ActionResponse? {
        return try {
            val proposal = proposalService.approveStrike(requestContext, proposalId)
            if (proposal.closed) {
                val updatedCount = getUpdatedCount(proposal)
                val strike: Strike = proposal.strike
                strike.count = updatedCount
                strikeService.updateStrike(strike)
            }
            null
        } catch (e: ProposerOfProposalException) {
            return ActionResponse.text(":hand: Sorry you are not allowed to vote. You are the proposer...")
        } catch (e: ProposalAlreadyClosedException) {
            return ActionResponse.text(":hand: Sorry you are not allowed to vote. This proposal is already closed...")
        }
    }

    private fun getUpdatedCount(proposal: StrikeProposal): Int {
        return if (proposal.modification == Modification.REMOVE) {
            proposal.strike.count - proposal.count
        } else {
            proposal.strike.count + proposal.count
        }
    }
}