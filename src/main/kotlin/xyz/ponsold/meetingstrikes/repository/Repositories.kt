package xyz.ponsold.meetingstrikes.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import xyz.ponsold.meetingstrikes.entity.Strike
import xyz.ponsold.meetingstrikes.entity.StrikeProposal
import xyz.ponsold.meetingstrikes.request.RequestContext
import java.util.*

interface StrikeRepository: JpaRepository<Strike, Long> {
    fun findByTeamIdAndChannelIdAndUserId(teamId: String, channelId: String, userId: String): Strike?
    fun findByTeamIdAndChannelId(teamId: String, channelId: String): List<Strike>
}

interface StrikeProposalRepository: JpaRepository<StrikeProposal, Long> {
    fun findAllByTeamIdAndChannelIdAndClosedFalse(teamId: String, channelId: String): List<StrikeProposal>
}