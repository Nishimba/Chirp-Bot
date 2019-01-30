package com.dbase;

import com.nish.BotUtils;

import com.youtube.libs;
        import com.youtube.libs.google.api.client.auth.oauth2.Credential;
        import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
        import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
        import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
        import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
        import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
        import com.google.api.client.googleapis.json.GoogleJsonResponseException;
        import com.google.api.client.http.HttpTransport;
        import com.google.api.client.json.jackson2.JacksonFactory;
        import com.google.api.client.json.JsonFactory;
        import com.google.api.client.util.store.FileDataStoreFactory;

        import com.google.api.services.youtube.YouTubeScopes;
        import com.google.api.services.youtube.model.*;
        import com.google.api.services.youtube.YouTube;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.util.Arrays;
        import java.util.Collection;
        import java.util.HashMap;
        import java.util.List;


public class YTParser {

    /** Application name. */
    private static final String APPLICATION_NAME = "API Sample";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/java-youtube-api-tests");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final Collection<String> SCOPES = Arrays.asList("YouTubeScopes.https://www.googleapis.com/auth/youtube.force-ssl YouTubeScopes.https://www.googleapis.com/auth/youtubepartner");

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = ApiExample.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader( in ));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized API client service, such as a YouTube
     * Data API client service.
     * @return an authorized API client service
     * @throws IOException
     */
    public static YouTube getYouTubeService() throws IOException {
        Credential credential = authorize();
        return new YouTube.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {

        YouTube youtube = getYouTubeService();

        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "snippet,contentDetails,statistics");
            parameters.put("id", "Ks-_Mh1QhMc");

            YouTube.Videos.List videosListByIdRequest = youtube.videos().list(parameters.get("part").toString());
            if (parameters.containsKey("id") && parameters.get("id") != "") {
                videosListByIdRequest.setId(parameters.get("id").toString());
            }

            VideoListResponse response = videosListByIdRequest.execute();
            System.out.println(response);


        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }



}
public class YTParser
{
    static String API_KEY= (BotUtils.ReadLines("res/APIKEY.txt")).get(0);


}