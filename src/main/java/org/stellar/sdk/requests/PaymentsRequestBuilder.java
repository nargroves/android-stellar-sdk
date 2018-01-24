package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;

import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.operations.OperationResponse;

import java.io.IOException;
import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds requests connected to payments.
 */
public class PaymentsRequestBuilder extends RequestBuilder {

    public PaymentsRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "payments");
    }

    /**
     * Builds request to <code>GET /accounts/{account}/payments</code>
     *
     * @param account Account for which to get payments
     * @see <a href="https://www.stellar.org/developers/horizon/reference/payments-for-account.html">Payments for Account</a>
     */
    public PaymentsRequestBuilder forAccount(KeyPair account) {
        account = checkNotNull(account, "account cannot be null");
        this.setSegments("accounts", account.getAccountId(), "payments");
        return this;
    }

    /**
     * Builds request to <code>GET /ledgers/{ledgerSeq}/payments</code>
     *
     * @param ledgerSeq Ledger for which to get payments
     * @see <a href="https://www.stellar.org/developers/horizon/reference/payments-for-ledger.html">Payments for Ledger</a>
     */
    public PaymentsRequestBuilder forLedger(long ledgerSeq) {
        this.setSegments("ledgers", String.valueOf(ledgerSeq), "payments");
        return this;
    }

    /**
     * Builds request to <code>GET /transactions/{transactionId}/payments</code>
     *
     * @param transactionId Transaction ID for which to get payments
     * @see <a href="https://www.stellar.org/developers/horizon/reference/payments-for-transaction.html">Payments for Transaction</a>
     */
    public PaymentsRequestBuilder forTransaction(String transactionId) {
        transactionId = checkNotNull(transactionId, "transactionId cannot be null");
        this.setSegments("transactions", transactionId, "payments");
        return this;
    }

    /**
     * Requests specific <code>uri</code> and returns {@link Page} of {@link OperationResponse}.
     * This method is helpful for getting the next set of results.
     *
     * @return {@link Page} of {@link OperationResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<OperationResponse> execute(URI uri) throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<Page<OperationResponse>>() {};
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().toString(), type.getType());
    }

    /**
     * Allows to stream SSE events from horizon.
     * Certain endpoints in Horizon can be called in streaming mode using Server-Sent Events.
     * This mode will keep the connection to horizon open and horizon will continue to return
     * responses as ledgers close.
     *
     * @param listener {@link EventListener} implementation with {@link OperationResponse} type
     * @return EventSource object, so you can <code>close()</code> connection when not needed anymore
     * @see <a href="http://www.w3.org/TR/eventsource/" target="_blank">Server-Sent Events</a>
     * @see <a href="https://www.stellar.org/developers/horizon/learn/responses.html" target="_blank">Response Format documentation</a>
     */
    public EventSource stream(final EventListener<OperationResponse> listener) {
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
                OperationResponse operation = GsonSingleton.getInstance()
                        .fromJson(messageEvent.getData(), OperationResponse.class);
                listener.onEvent(operation);
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
     * @return {@link Page} of {@link OperationResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<OperationResponse> execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }

    @Override
    public PaymentsRequestBuilder cursor(String token) {
        super.cursor(token);
        return this;
    }

    @Override
    public PaymentsRequestBuilder limit(int number) {
        super.limit(number);
        return this;
    }

    @Override
    public PaymentsRequestBuilder order(Order direction) {
        super.order(direction);
        return this;
    }
}
