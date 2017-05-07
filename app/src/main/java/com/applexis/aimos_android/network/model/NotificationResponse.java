package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

import java.util.Date;
import java.util.List;

public class NotificationResponse extends ResponseBase {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN,
        DATABASE_ERROR
    }

    public enum Type {
        ADDED_TO_FRIEND_LIST,
        GET_MESSAGE,
        INVITED_TO_DIALOG,
        DIALOG_EXCLUDE,
        DIALOG_RENAMED
    }

    class NotificationMinimal {
        private String id;
        private String notificationType;
        private Long idDialogFrom;
        private String dialogName;
        private UserMinimalInfo userFrom;
        private String datetime;

        public NotificationMinimal(Long id, String notificationType, Long idDialogFrom, String dialogName, UserMinimalInfo userFrom, Date datetime, AESCrypto aes) {
            this.id = aes.encrypt(String.valueOf(id));
            this.notificationType = notificationType;
            this.idDialogFrom = idDialogFrom;
            this.dialogName = dialogName;
            this.userFrom = userFrom;
            this.datetime = aes.encrypt(String.valueOf(datetime.getTime()));
        }

        public Long getId(AESCrypto aes) {
            return Long.valueOf(aes.decrypt(id));
        }

        public void setId(Long id, AESCrypto aes) {
            this.id = aes.encrypt(String.valueOf(id));
        }

        public Date getDatetime(AESCrypto aes) {
            return new Date(Long.valueOf(aes.decrypt(datetime)));
        }

        public void setDatetime(Date datetime, AESCrypto aes) {
            this.datetime = aes.encrypt(String.valueOf(datetime.getTime()));
        }
    }

    private List<NotificationMinimal> notifications;

    public NotificationResponse() {
    }

    public NotificationResponse(String success, String errorType, List<NotificationMinimal> notifications) {
        super(success, errorType);
        this.notifications = notifications;
    }

    public NotificationResponse(AESCrypto aes) {
        super(aes);
    }

    public NotificationResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public List<NotificationMinimal> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationMinimal> notifications) {
        this.notifications = notifications;
    }
}
