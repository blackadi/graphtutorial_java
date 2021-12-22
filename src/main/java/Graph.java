import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;

public class Graph {
    private static GraphServiceClient<Request> graphClient = null;
    private static TokenCredentialAuthProvider authProvider = null;

    public static void initializeGraphAuth(String CLIENT_ID, String CLIENT_SECRET, String TENANT_GUID,
            List<String> scopes) {

        final int proxyPort = 3128;
        final InetSocketAddress proxyInetAddress = new InetSocketAddress("192.168.1.122", proxyPort);

        // The section below configures the proxy for the Azure Identity client
        // and is only needed if you rely on Azure Identity for authentication
        final ProxyOptions pOptions = new ProxyOptions(ProxyOptions.Type.HTTP, proxyInetAddress);
        final HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setProxyOptions(pOptions);
        final HttpClient azHttpClient = HttpClient.createDefault(clientOptions); // Try this one

        // Create the auth provider
        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_GUID)
                // don't forget that addition to use the configured client
                .httpClient(azHttpClient) // Comment this to disable proxy
                .build();

        authProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);

        // The section below configures the proxy for the Microsoft Graph SDK client
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInetAddress);

        final OkHttpClient graphHttpClient = HttpClients.createDefault(authProvider)
                .newBuilder()
                .proxy(proxy)
                .build();

        // Create default logger to only log errors
        DefaultLogger logger = new DefaultLogger();
        logger.setLoggingLevel(LoggerLevel.ERROR);

        // Build a Graph client
        graphClient = GraphServiceClient.builder()
                // .authenticationProvider(authProvider) // Uncomment this if no proxy is used
                .httpClient(graphHttpClient) // Comment this to disable proxy
                .logger(logger)
                .buildClient();
    }

    public static String getUserAccessToken() {
        try {
            URL meUrl = new URL("https://graph.microsoft.com/v1.0/me");
            return authProvider.getAuthorizationTokenAsync(meUrl).get();
        } catch (Exception ex) {
            return null;
        }
    }

    public static UserCollectionPage getUser() {
        if (graphClient == null)
            throw new NullPointerException(
                    "Graph client has not been initialized. Call initializeGraphAuth before calling this method");

        // GET /me to get authenticated user
        UserCollectionPage users = graphClient
                .users()
                .buildRequest()
                // .select("displayName,JobTitle")
                .top(7)
                .get();

        return users;
    }
}
