// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.UserCollectionPage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

class ClientCredentialGrant {

    private static String tenantId;
    private static String clientId;
    private static String secret;
    private static List<String> scope;

    public static void main(String args[]) throws Exception {

        System.out.println("Java Graph Tutorial");
        System.out.println();

        setUpSampleData();

        // Initialize Graph with auth settings
        Graph.initializeGraphAuth(clientId, secret, tenantId, scope);
        final String accessToken = Graph.getUserAccessToken();

        System.out.println("Access token: " + accessToken);

        // Greet the user
        UserCollectionPage user = Graph.getUser();
        List<User> users = user.getCurrentPage();
        for (User user2 : users) {
            System.out.println("Welcome " + user2.displayName);
            System.out.println("mail: " + user2.userPrincipalName);
            System.out.println();
        }
    }

    /**
     * Helper function unique to this sample setting. In a real application these
     * wouldn't be so hardcoded, for example
     * different users may need different authority endpoints or scopes
     */
    private static void setUpSampleData() throws IOException {
        // Load properties file and set properties used throughout the sample
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        tenantId = properties.getProperty("TENANT_ID");
        clientId = properties.getProperty("CLIENT_ID");
        secret = properties.getProperty("SECRET");
        scope = Arrays
                .asList(properties.getProperty("SCOPE"));
    }
}
