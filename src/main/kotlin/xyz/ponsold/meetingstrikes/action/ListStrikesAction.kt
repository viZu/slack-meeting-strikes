package xyz.ponsold.meetingstrikes.action

import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.block.element.OverflowMenuElement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.ponsold.meetingstrikes.dto.UserStrike
import xyz.ponsold.meetingstrikes.request.RequestContext
import xyz.ponsold.meetingstrikes.service.StrikeService
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.transaction.Transactional

@Service
class ListStrikesAction(@Autowired val strikeService: StrikeService) {

    @Transactional
    fun listStrikes(groupId: RequestContext): ActionResponse {
        val strikes: List<UserStrike> = strikeService.getStrikes(groupId)
        val blocks = strikes.stream().map { userStrike: UserStrike -> toLayoutBlock(userStrike) }.collect(Collectors.toList())
        return if (blocks.isEmpty()) {
            ActionResponse.text("There are no strikes...")
        } else {
            ActionResponse.blocks(blocks)
        }
    }

    private fun toLayoutBlock(userStrike: UserStrike): LayoutBlock {
        return SectionBlock(null, "strike-" + userStrike.userId, listOf(
                userStrike.userId.markdown(),
                getChocolateBars(userStrike)
        ), getOverflowMenu(userStrike))
    }

    private fun getChocolateBars(userStrike: UserStrike): PlainTextObject {
        val count: Int = userStrike.strikeCount - userStrike.pendingCountRemove
        val chocolateStrings: String = getChocolateBars(count)
        return "$chocolateStrings ${getPendingString(userStrike)}".plain()
    }

    private fun getPendingString(userStrike: UserStrike): String {
        val remove = if (userStrike.pendingCountRemove > 0) java.lang.String.format("(-%s)", getChocolateBars(userStrike.pendingCountRemove)) else ""
        val add = if (userStrike.pendingCountAdd > 0) java.lang.String.format("(+%s)", getChocolateBars(userStrike.pendingCountAdd)) else ""
        return "$remove$add".trim()
    }

    private fun getChocolateBars(count: Int): String {
        return IntStream.range(0, count).mapToObj { ":chocolate_bar:" }.collect(Collectors.joining())
    }

    private fun getOverflowMenu(userStrike: UserStrike): OverflowMenuElement {
        return OverflowMenuElement.builder()
                .actionId("list-overflow-actions")
                .options(getOptions(userStrike))
                .build()
    }

    private fun getOptions(userStrike: UserStrike): List<OptionObject> {
        val options: MutableList<OptionObject> = ArrayList()
        options.add(
                OptionObject.builder()
                        .text("Add strike".plain())
                        .description("Adds a new Strike to this user".plain())
                        .value(ActionId.LIST_OVERFLOW_ACTIONS_ADD.id(userStrike.userId))
                        .build()
        )
        if (userStrike.strikeCount > 0) {
            options.add(
                    OptionObject.builder()
                            .text("Remove strike".plain())
                            .description("Removes an existing strike from this user".plain())
                            .value(ActionId.LIST_OVERFLOW_ACTIONS_REMOVE.id(userStrike.userId))
                            .build()
            )
        }
        return options
    }
}