package com.docker.utils;

import chat.logs.LoggerEx;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Royal Chan (cn.royalchan@gmail.com) on 9/17/2018.
 */
public class GmailTransmitter {
    private static final String TAG = GmailTransmitter.class.getSimpleName();

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static NetHttpTransport HTTP_TRANSPORT = null;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.info(TAG, "Get GoogleNetHttpTransport error: " + e.getMessage());
        }
    }

    private Gmail service;

    private static final Collection GMAIL_SCOPE = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM, GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_COMPOSE, GmailScopes.GMAIL_MODIFY);

    private GmailTransmitter() {
    }

    /**
     * Create GmailTransmitter using Gmail api.
     * Credential with Google Service Account
     * @param serviceAccountId Google Service Account Id like XXX@XXX.iam.gserviceaccount.com
     * @param serviceAccountPrivateKey A p12 file to authorize
     * @param serviceAccountUser Google Service Account User
     * @param applicationName Google Api application name
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public GmailTransmitter(String serviceAccountId, File serviceAccountPrivateKey, String serviceAccountUser, String applicationName) throws GeneralSecurityException, IOException {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setServiceAccountId(serviceAccountId)
                .setServiceAccountPrivateKeyFromP12File(serviceAccountPrivateKey)
                .setServiceAccountUser(serviceAccountUser)
                .setServiceAccountScopes(GMAIL_SCOPE)
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .build();

        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    private static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Send message.
     * @return Send result
     * @throws IOException If the credentials.json file cannot be found.
     */
    public String send(MimeMessage message) throws IOException, MessagingException {
        Message encodeMessage = createMessageWithEmail(message);
        String user = "me";
        Message result = service.users().messages().send(user, encodeMessage).execute();
        return result.toString();
    }
}
