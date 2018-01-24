package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;

import org.apache.http.client.fluent.Request;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.LedgerResponse;
import org.stellar.sdk.responses.Page;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Builds requests connected to ledgers.
 */
public class LedgersRequestBuilder extends RequestBuilder {

    public LedgersRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "ledgers");
    }

    /**
     * Requests specific <code>uri</code> and returns {@link LedgerResponse}.
     * This method is helpful for getting the links.
     *
     * @throws IOException
     */
    public LedgerResponse ledger(URI uri) throws IOException {
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().toString(), LedgerResponse.class);
    }

    /**
     * Requests <code>GET /ledgers/{ledgerSeq}</code>
     *
     * @param ledgerSeq Ledger to fetch
     * @throws IOException
     * @see <a href="https://www.stellar.org/developers/horizon/reference/ledgers-single.html">Ledger Details</a>
     */
    public LedgerResponse ledger(long ledgerSeq) throws IOException {
        this.setSegments("ledgers", String.valueOf(ledgerSeq));
        return this.ledger(this.buildUri());
    }

    /**
     * Requests specific <code>uri</code> and returns {@link Page} of {@link LedgerResponse}.
     * This method is helpful for getting the next set of results.
     *
     * @return {@link Page} of {@link LedgerResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<LedgerResponse> execute(URI uri) throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<Page<LedgerResponse>>() {
        };
        ResponseHandler<Page<LedgerResponse>> responseHandler = new ResponseHandler<Page<LedgerResponse>>(type);
        return (Page<LedgerResponse>) Request.Get(uri).execute().handleResponse(responseHandler);
    }

    /**
     * Allows to stream SSE events from horizon.
     * Certain endpoints in Horizon can be called in streaming mode using Server-Sent Events.
     * This mode will keep the connection to horizon open and horizon will continue to return
     * responses as ledgers close.
     *
     * @param listener {@link EventListener} implementation with {@link LedgerResponse} type
     * @return EventSource object, so you can <code>close()</code> connection when not needed anymore
     * @see <a href="http://www.w3.org/TR/eventsource/" target="_blank">Server-Sent Events</a>
     * @see <a href="https://www.stellar.org/developers/horizon/learn/responses.html" target="_blank">Response Format documentation</a>
     */
    public EventSource stream(final EventListener<LedgerResponse> listener) {
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
                LedgerResponse ledger = GsonSingleton.getInstance()
                        .fromJson(messageEvent.getData(), LedgerResponse.class);
                listener.onEvent(ledger);
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
     * Build and execute request.
     *
     * @return {@link Page} of {@link LedgerResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<LedgerResponse> execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }

    @Override
    public LedgersRequestBuilder cursor(String token) {
        super.cursor(token);
        return this;
    }

    @Override
    public LedgersRequestBuilder limit(int number) {
        super.limit(number);
        return this;
    }

    @Override
    public LedgersRequestBuilder order(Order direction) {
        super.order(direction);
        return this;
    }
}
