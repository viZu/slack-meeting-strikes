package xyz.ponsold.meetingstrikes.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.dto.PendingApproval
import xyz.ponsold.meetingstrikes.dto.UserApproval
import xyz.ponsold.meetingstrikes.entity.*
import xyz.ponsold.meetingstrikes.repository.StrikeProposalRepository
import xyz.ponsold.meetingstrikes.request.RequestContext
import java.time.Instant
import kotlin.math.abs

@Service
class ProposalService(@Autowired val strikeProposalRepository: StrikeProposalRepository) {
    fun getPendingApprovals(groupId: RequestContext): List<PendingApproval> {
        return strikeProposalRepository.findAllByTeamIdAndChannelIdAndClosedFalse(groupId.teamId, groupId.channelId).map { pr ->
            val reviewers: Map<ReviewOutcome, List<UserApproval>> = pr.reviewers
                    .groupBy(keySelector = { it.outcome },
                            valueTransform = { UserApproval(it.userId, it.timestamp) })
            val approvals: List<UserApproval> = reviewers[ReviewOutcome.APPROVED] ?: listOf()
            val declines: List<UserApproval> = reviewers[ReviewOutcome.DECLINED] ?: listOf()
            PendingApproval(pr.id, pr.strike.userId, pr.proposerUserId, pr.count, pr.modification, Instant.now(), approvals, declines)
        }
    }

    private fun getUserApprovals(reviewer: Reviewer): UserApproval {
        val username: String = reviewer.userId
        return UserApproval(username, reviewer.timestamp)
    }

    fun addToStrikes(requestContext: RequestContext, strike: Strike, count: Int): StrikeProposal {
        val strikeProposal: StrikeProposal = createProposal(requestContext, strike, count)
        strikeProposal.modification = Modification.ADD
        return strikeProposalRepository.save(strikeProposal)
    }

    fun removeFromStrikes(requestContext: RequestContext, strike: Strike, count: Int): StrikeProposal {
        val proposal: StrikeProposal = createProposal(requestContext, strike, count)
        proposal.modification = Modification.REMOVE
        return strikeProposalRepository.save(proposal)
    }

    private fun createProposal(requestContext: RequestContext, strike: Strike, count: Int): StrikeProposal {
        return StrikeProposal(requestContext.teamId, requestContext.channelId, strike,
                Modification.ADD, count, requestContext.userId, false)
    }

    fun approveStrike(requestContext: RequestContext, proposalId: Long): StrikeProposal {
        return submitReview(requestContext, proposalId, ReviewOutcome.APPROVED)
    }

    fun declineStrike(requestContext: RequestContext, proposalId: Long): StrikeProposal {
        return submitReview(requestContext, proposalId, ReviewOutcome.DECLINED)
    }

    private fun submitReview(requestContext: RequestContext, proposalId: Long, reviewOutcome: ReviewOutcome): StrikeProposal {
        // TODO: throw IllegalArgumentException
        val proposal: StrikeProposal = strikeProposalRepository.findByIdOrNull(proposalId)
                ?: throw IllegalArgumentException()

        if (proposal.closed) {
            throw ProposalAlreadyClosedException()
        } else if (proposal.proposerUserId == requestContext.userId) {
            throw ProposerOfProposalException()
        }

        val reviewer = Reviewer(requestContext.userId, reviewOutcome, proposal)
        val reviewers: MutableList<Reviewer> = proposal.reviewers
        reviewers.add(reviewer)
        val countApproved = reviewers.filter { it.outcome == ReviewOutcome.APPROVED }.count()
        val countDeclined = reviewers.filter { it.outcome == ReviewOutcome.DECLINED }.count()
        val closed = abs(countApproved - countDeclined) >= 2
        proposal.closed = (closed)
        return strikeProposalRepository.save(proposal)
    }
}