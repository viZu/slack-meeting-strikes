package xyz.ponsold.meetingstrikes.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.dto.UserStrike
import xyz.ponsold.meetingstrikes.entity.Modification
import xyz.ponsold.meetingstrikes.entity.Strike
import xyz.ponsold.meetingstrikes.repository.StrikeRepository
import xyz.ponsold.meetingstrikes.request.RequestContext

@Service
class StrikeService(@Autowired val strikeRepository: StrikeRepository) {

    fun getStrikes(requestContext: RequestContext): List<UserStrike> {
        val strikes: List<Strike> = strikeRepository.findByTeamIdAndChannelId(requestContext.teamId, requestContext.channelId)
        return strikes.map { s ->
            val addCount: Int = s.getOpenProposals()
                    .filter { it.modification == Modification.ADD }
                    .map { it.count }
                    .sum()

            val removeCount: Int = s.getOpenProposals()
                    .filter { it.modification == Modification.REMOVE }
                    .map { it.count }
                    .sum()
            UserStrike(s.id, s.userId, s.count, addCount, removeCount)
        }
    }

    fun createOrGetStrike(requestContext: RequestContext, userId: String): Strike {
        return strikeRepository.findByTeamIdAndChannelIdAndUserId(requestContext.teamId, requestContext.channelId, userId) ?: createNewStrike(requestContext, userId)
    }

    private fun createNewStrike(requestContext: RequestContext, userId: String): Strike {
        val strike = Strike(requestContext.teamId, requestContext.teamId, userId, 0)
        return strikeRepository.save(strike)
    }

    fun updateStrike(strike: Strike) {
        strikeRepository.save(strike)
    }
}
