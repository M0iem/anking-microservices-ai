
package ma.enset.ebankbot.discord;

import ma.enset.ebankbot.agents.EbankAIAgent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class DiscordBot extends ListenerAdapter {

    private final EbankAIAgent ebankAIAgent;

    public DiscordBot(EbankAIAgent ebankAIAgent) {
        this.ebankAIAgent = ebankAIAgent;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // Ignore messages sent by bots
        if (event.getAuthor().isBot()) return;

        // Get user message
        String query = event.getMessage().getContentRaw();

        if (query.isBlank()) return;

        // Unique conversation for each Discord user
        String conversationId = "discord-" + event.getAuthor().getId();

        // Send message to AI Agent
        String response = ebankAIAgent.chat(new Prompt(query));

        // Send AI response to Discord
        event.getChannel().sendMessage(response).queue();
    }
}
