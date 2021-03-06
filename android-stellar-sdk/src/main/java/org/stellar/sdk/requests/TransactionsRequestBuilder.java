package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;

import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.TransactionResponse;

import java.io.IOException;
import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds requests connected to transactions.
 */
public class TransactionsRequestBuilder extends RequestBuilder {

    public TransactionsRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "transactions");
    }

    /**
     * Requests specific <code>uri</code> and returns {@link TransactionResponse}.
     * This method is helpful for getting the links.
     *
     * @throws IOException
     */
    public TransactionResponse transaction(URI uri) throws IOException {
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().string(), TransactionResponse.class);
    }

    /**
     * Requests <code>GET /transactions/{transactionId}</code>
     *
     * @param transactionId Transaction to fetch
     * @throws IOException
     * @see <a href="https://www.stellar.org/developers/horizon/reference/transactions-single.html">Transaction Details</a>
     */
    public TransactionResponse transaction(String transactionId) throws IOException {
        this.setSegments("transactions", transactionId);
        return this.transaction(this.buildUri());
    }

    /**
     * Builds request to <code>GET /accounts/{account}/transactions</code>
     *
     * @param account Account for which to get transactions
     * @see <a href="https://www.stellar.org/developers/horizon/reference/transactions-for-account.html">Transactions for Account</a>
     */
    public TransactionsRequestBuilder forAccount(KeyPair account) {
        account = checkNotNull(account, "account cannot be null");
        this.setSegments("accounts", account.getAccountId(), "transactions");
        return this;
    }

    /**
     * Builds request to <code>GET /ledgers/{ledgerSeq}/transactions</code>
     *
     * @param ledgerSeq Ledger for which to get transactions
     * @see <a href="https://www.stellar.org/developers/horizon/reference/transactions-for-ledger.html">Transactions for Ledger</a>
     */
    public TransactionsRequestBuilder forLedger(long ledgerSeq) {
        this.setSegments("ledgers", String.valueOf(ledgerSeq), "transactions");
        return this;
    }

    /**
     * Requests specific <code>uri</code> and returns {@link Page} of {@link TransactionResponse}.
     * This method is helpful for getting the next set of results.
     *
     * @return {@link Page} of {@link TransactionResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<TransactionResponse> execute(URI uri) throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<Page<TransactionResponse>>() {};
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().string(), type.getType());
    }

    /**
     * Allows to stream SSE events from horizon.
     * Certain endpoints in Horizon can be called in streaming mode using Server-Sent Events.
     * This mode will keep the connection to horizon open and horizon will continue to return
     * responses as ledgers close.
     *
     * @param listener {@link EventListener} implementation with {@link TransactionResponse} type
     * @return EventSource object, so you can <code>close()</code> connection when not needed anymore
     * @see <a href="http://www.w3.org/TR/eventsource/" target="_blank">Server-Sent Events</a>
     * @see <a href="https://www.stellar.org/developers/horizon/learn/responses.html" target="_blank">Response Format documentation</a>
     */
    public EventSource stream(final EventListener<TransactionResponse> listener) {
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
                TransactionResponse transaction = GsonSingleton.getInstance()
                        .fromJson(messageEvent.getData(), TransactionResponse.class);
                listener.onEvent(transaction);
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
     * @return {@link Page} of {@link TransactionResponse}
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<TransactionResponse> execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }

    @Override
    public TransactionsRequestBuilder cursor(String token) {
        super.cursor(token);
        return this;
    }

    @Override
    public TransactionsRequestBuilder limit(int number) {
        super.limit(number);
        return this;
    }

    @Override
    public TransactionsRequestBuilder order(Order direction) {
        super.order(direction);
        return this;
    }
}
