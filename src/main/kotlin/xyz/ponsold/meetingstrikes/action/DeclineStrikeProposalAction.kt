package xyz.ponsold.meetingstrikes.action

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.request.RequestContext
import xyz.ponsold.meetingstrikes.service.ProposalAlreadyClosedException
import xyz.ponsold.meetingstrikes.service.ProposalService
import xyz.ponsold.meetingstrikes.service.ProposerOfProposalException
import java.util.*
import javax.transaction.Transactional

@Service
class DeclineStrikeProposalAction(@Autowired val proposalService: ProposalService) {

    @Transactional
    fun decline(requestContext: RequestContext, proposalId: Long): ActionResponse? {
        return try {
            proposalService.declineStrike(requestContext, proposalId)
            null
        } catch (e: ProposerOfProposalException) {
            ActionResponse.text(":hand: Sorry you are not allowed to vote. You are the proposer...")
        } catch (e: ProposalAlreadyClosedException) {
            ActionResponse.text(":hand: Sorry you are not allowed to vote. This proposal is already closed...")
        }
    }
}