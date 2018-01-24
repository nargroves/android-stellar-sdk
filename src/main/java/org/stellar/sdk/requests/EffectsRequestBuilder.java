package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;

import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.effects.EffectResponse;

import java.io.IOException;
import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds requests connected to effects.
 */
public class EffectsRequestBuilder extends RequestBuilder {
    
    public EffectsRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "effects");
    }

    /**
     * Builds request to <code>GET /accounts/{account}/effects</code>
     *
     * @param account Account for which to get effects
     * @see <a href="https://www.stellar.org/developers/horizon/reference/effects-for-account.html">Effects for Account</a>
     */
    public EffectsRequestBuilder forAccount(KeyPair account) {
        account = checkNotNull(account, "account cannot be null");
        this.setSegments("accounts", account.getAccountId(), "effects");
        return this;
    }

    /**
     * Builds request to <code>GET /ledgers/{ledgerSeq}/effects</code>
     *
     * @param ledgerSeq Ledger for which to get effects
     * @see <a href="https://www.stellar.org/developers/horizon/reference/effects-for-ledger.html">Effects for Ledger</a>
     */
    public EffectsRequestBuilder forLedger(long ledgerSeq) {
        this.setSegments("ledgers", String.valueOf(ledgerSeq), "effects");
        return this;
    }

    /**
     * Builds request to <code>GET /transactions/{transactionId}/effects</code>
     *
     * @param transactionId Transaction ID for which to get effects
     * @see <a href="https://www.stellar.org/developers/horizon/reference/effects-for-transaction.html">Effect for Transaction</a>
     */
    public EffectsRequestBuilder forTransaction(String transactionId) {
        transactionId = checkNotNull(transactionId, "transactionId cannot be null");
        this.setSegments("transactions", transactionId, "effects");
        return this;
    }

    /**
     * Builds request to <code>GET /operation/{operationId}/effects</code>
     *
     * @param operationId Operation ID for which to get effects
     * @see <a href="https://www.stellar.org/developers/horizon/reference/effects-for-operation.html">Effect for Operation</a>
     */
    public EffectsRequestBuilder forOperation(long operationId) {
        this.setSegments("operations", String.valueOf(operationId), "effects");
        return this;
    }

    /**
     * Requests specific <code>uri</code> and returns {@link Page} of {@link EffectResponse}.
     * This method is helpful for getting the next set of results.
     *
     * @return {@link Page} of {@link EffectResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<EffectResponse> execute(URI uri) throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<Page<EffectResponse>>() {};
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().string(), type.getType());
    }

    /**
     * Allows to stream SSE events from horizon.
     * Certain endpoints in Horizon can be called in streaming mode using Server-Sent Events.
     * This mode will keep the connection to horizon open and horizon will continue to return
     * responses as ledgers close.
     *
     * @param listener {@link EventListener} implementation with {@link EffectResponse} type
     * @return EventSource object, so you can <code>close()</code> connection when not needed anymore
     * @see <a href="http://www.w3.org/TR/eventsource/" target="_blank">Server-Sent Events</a>
     * @see <a href="https://www.stellar.org/developers/horizon/learn/responses.html" target="_blank">Response Format documentation</a>
     */
    public EventSource stream(final EventListener<EffectResponse> listener) {
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
                EffectResponse effect = GsonSingleton.getInstance()
                        .fromJson(messageEvent.getData(), EffectResponse.class);
                listener.onEvent(effect);
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
     * @return {@link Page} of {@link EffectResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<EffectResponse> execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }

    @Override
    public EffectsRequestBuilder cursor(String token) {
        super.cursor(token);
        return this;
    }

    @Override
    public EffectsRequestBuilder limit(int number) {
        super.limit(number);
        return this;
    }

    @Override
    public EffectsRequestBuilder order(Order direction) {
        super.order(direction);
        return this;
    }
}
