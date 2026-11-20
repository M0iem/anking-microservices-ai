
package ma.enset.ebankbot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.File;

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot implements SpringLongPollingBot,
        LongPollingSingleThreadUpdateConsumer {

    private final String telegramBotToken;

    private final TelegramClient telegramClient;

    // Constructor
    public TelegramBot(
            @Value("${telegram.token}") String telegramBotToken) {

        this.telegramBotToken = telegramBotToken;

        this.telegramClient =
                new OkHttpTelegramClient(telegramBotToken);
    }

    // Return Telegram Bot Token
    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    // Register update consumer
    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    // Receive Telegram messages
    @Override
    public void consume(Update telegramRequest) {

        try {

            if (!telegramRequest.hasMessage()) return;

            // Get message text
            String messageText = null;

            if (telegramRequest.getMessage().hasText()) {
                messageText =
                        telegramRequest.getMessage().getText();
            }

            // Get Chat ID
            Long chatId =
                    telegramRequest.getMessage().getChatId();

            // Get photos
            List<PhotoSize> photos =
                    telegramRequest.getMessage().getPhoto();

            List<String> mediaList = new ArrayList<>();

            String caption = null;

            // Check if message contains photos
            if (photos != null && !photos.isEmpty()) {

                caption =
                        telegramRequest.getMessage().getCaption();

                if (caption == null) {
                    caption = "What do you see in this image";
                }

                // Get largest photo
                PhotoSize ps = photos.get(photos.size() - 1);

                String fileId = ps.getFileId();

                // Get Telegram file
                GetFile getFile = new GetFile(fileId);

                File file = telegramClient.execute(getFile);

                // Get image URL
                String fileUrl =
                        file.getFileUrl(telegramBotToken);

                mediaList.add(fileUrl);
            }

            // Display received information
            System.out.println("Chat ID: " + chatId);
            System.out.println("Message: " + messageText);
            System.out.println("Caption: " + caption);
            System.out.println("Media: " + mediaList);

            // Prepare response
            String responseText;

            if (!mediaList.isEmpty()) {

                responseText =
                        "Image received successfully!";

            } else if (messageText != null) {

                responseText =
                        "Message received: " + messageText;

            } else {

                responseText =
                        "I received your message!";
            }

            // Send response
            SendMessage response = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(responseText)
                    .build();

            telegramClient.execute(response);

        } catch (TelegramApiException e) {

            System.err.println(
                    "Telegram API Error: " + e.getMessage()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error processing Telegram update: "
                            + e.getMessage()
            );
        }
    }
}
