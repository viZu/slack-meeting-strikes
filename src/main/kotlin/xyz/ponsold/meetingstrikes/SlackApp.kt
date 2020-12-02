package xyz.ponsold.meetingstrikes

import com.slack.api.bolt.App
import com.slack.api.bolt.servlet.SlackAppServlet
import javax.servlet.annotation.WebServlet

@WebServlet("/slack/events")
class SlackolateApp(app: App) : SlackAppServlet(app)