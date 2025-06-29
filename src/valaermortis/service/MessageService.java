package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.MessageDao;
import valaermortis.model.Message;
import valaermortis.util.TerminalArt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageService {
    private final AppContext ctx;
    private final MessageDao messageDao = new MessageDao();

    public MessageService(AppContext ctx) {
        this.ctx = ctx;
    }

    public void sendMessage(String userId, String title, String message) {
        messageDao.addMessage(userId, title, message);
    }

    public void sendMessage(String title, String message) {
        String userId = ctx.getCurrentUserId();
        if (userId != null) {
            sendMessage(userId, title, message);
        }
    }

    public void displayMessages() {
        String userId = ctx.getCurrentUserId();
        if (userId == null)
            return;

        List<Message> messages = messageDao.getRecentMessages(userId, 50);

        if (messages.isEmpty()) {
            System.out.println("No messages yet.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            LocalDateTime createdAt = message.getCreatedAt().toLocalDateTime();

            System.out.println("\n" + TerminalArt.yellow("= " + message.getTitle() + " ="));
            System.out.println(message.getMessage());
            System.out.println(TerminalArt.cyan("Time: " + createdAt.format(formatter)));
            System.out.println("-".repeat(50));
        }
    }

    public int getMessageCount() {
        String userId = ctx.getCurrentUserId();
        if (userId == null)
            return 0;
        return messageDao.getMessageCount(userId);
    }

    public void cleanOldMessages(int daysOld) {
        String userId = ctx.getCurrentUserId();
        if (userId != null) {
            messageDao.deleteOldMessages(userId, daysOld);
        }
    }

    public void sendBuildingCompletedMessage(String buildingType, int level) {
        String title = "Building Upgrade Completed";
        String message = buildingType.toUpperCase() + " has been upgraded to level " + level + "!";
        sendMessage(title, message);
    }

    public void sendMissionCompletedMessage(String missionType, String details) {
        String title = "Mission Completed";
        String message = missionType.toUpperCase() + " MISSION " + details;
        sendMessage(title, message);
    }

    public void sendTrainingCompletedMessage(String unitType, int quantity) {
        String title = "Training Completed";
        String message = quantity + " " + unitType + "(s) training completed and ready for battle!";
        sendMessage(title, message);
    }

    public void sendSystemMessage(String title, String message) {
        sendMessage("[SYSTEM] " + title, message);
    }
}
