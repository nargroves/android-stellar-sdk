package org.stellar.sdk.requests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;

import org.stellar.sdk.KeyPair;
import org.stellar.sdk.federation.FederationResponse;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.Page;

import java.io.IOException;
import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Builds requests connected to accounts.
 */
public class AccountsRequestBuilder extends RequestBuilder {

    public AccountsRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "accounts");
    }

    /**
     * Requests specific <code>uri</code> and returns {@link AccountResponse}.
     * This method is helpful for getting the links.
     *
     * @throws IOException
     */
    public AccountResponse account(URI uri) throws IOException {
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().toString(), AccountResponse.class);
    }

    /**
     * Requests <code>GET /accounts/{account}</code>
     *
     * @param account Account to fetch
     * @throws IOException
     * @see <a href="https://www.stellar.org/developers/horizon/reference/accounts-single.html">Account Details</a>
     */
    public AccountResponse account(KeyPair account) throws IOException {
        this.setSegments("accounts", account.getAccountId());
        return this.account(this.buildUri());
    }

    /**
     * Requests specific <code>uri</code> and returns {@link Page} of {@link AccountResponse}.
     * This method is helpful for getting the next set of results.
     *
     * @return {@link Page} of {@link AccountResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<AccountResponse> execute(URI uri) throws IOException, TooManyRequestsException {
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().toString(), Page<AccountResponse>.class);
    }

    /**
     * Allows to stream SSE events from horizon.
     * Certain endpoints in Horizon can be called in streaming mode using Server-Sent Events.
     * This mode will keep the connection to horizon open and horizon will continue to return
     * responses as ledgers close.
     *
     * @param listener {@link EventListener} implementation with {@link AccountResponse} type
     * @return EventSource object, so you can <code>close()</code> connection when not needed anymore
     * @see <a href="http://www.w3.org/TR/eventsource/" target="_blank">Server-Sent Events</a>
     * @see <a href="https://www.stellar.org/developers/horizon/learn/responses.html" target="_blank">Response Format documentation</a>
     */
    public EventSource stream(final EventListener<AccountResponse> listener) {
        EventHandler handler = new EventHandler() {
            @Override
            public void onOpen() throws Exception {

            }

            @Override
            public void onClosed() throws Exception {

            }

            @Override
            public void onMessage(String event, MessageEvent messageEvent) throws Exception {
                if (messageEvent.getData().equals("\"hello\"")) {
                    return;
                }
                AccountResponse account = GsonSingleton.getInstance()
                        .fromJson(messageEvent.getData(), AccountResponse.class);
                listener.onEvent(account);
            }

            @Override
            public void onComment(String comment) throws Exception {

            }

            @Override
            public void onError(Throwable t) {

            }
        };

        return new EventSource.Builder(handler, this.buildUri()).client(httpClient).build();
    }

    /**
     * Build and execute request. <strong>Warning!</strong> {@link AccountResponse}s in {@link Page} will contain only <code>keypair</code> field.
     *
     * @return {@link Page} of {@link AccountResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<AccountResponse> execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }

    @Override
    public AccountsRequestBuilder cursor(String token) {
        super.cursor(token);
        return this;
    }

    @Override
    public AccountsRequestBuilder limit(int number) {
        super.limit(number);
        return this;
    }

    @Override
    public AccountsRequestBuilder order(Order direction) {
        super.order(direction);
        return this;
    }
}
