package org.stellar.sdk;

import org.apache.commons.io.IOUtils;
import org.stellar.sdk.requests.*;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Main class used to connect to Horizon server.
 */
public class Server {

    private URI serverURI;
    private OkHttpClient httpClient = new OkHttpClient();

    public Server(String uri) {
        try {
            serverURI = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns {@link AccountsRequestBuilder} instance.
     */
    public AccountsRequestBuilder accounts() {
        return new AccountsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link EffectsRequestBuilder} instance.
     */
    public EffectsRequestBuilder effects() {
        return new EffectsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link LedgersRequestBuilder} instance.
     */
    public LedgersRequestBuilder ledgers() {
        return new LedgersRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link OffersRequestBuilder} instance.
     */
    public OffersRequestBuilder offers() {
        return new OffersRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link OperationsRequestBuilder} instance.
     */
    public OperationsRequestBuilder operations() {
        return new OperationsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link OrderBookRequestBuilder} instance.
     */
    public OrderBookRequestBuilder orderBook() {
        return new OrderBookRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link TradesRequestBuilder} instance.
     */
    public TradesRequestBuilder trades() {
        return new TradesRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link PathsRequestBuilder} instance.
     */
    public PathsRequestBuilder paths() {
        return new PathsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link PaymentsRequestBuilder} instance.
     */
    public PaymentsRequestBuilder payments() {
        return new PaymentsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link TransactionsRequestBuilder} instance.
     */
    public TransactionsRequestBuilder transactions() {
        return new TransactionsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Submits transaction to the network.
     *
     * @param transaction transaction to submit to the network.
     * @return {@link SubmitTransactionResponse}
     * @throws IOException
     */
    public SubmitTransactionResponse submitTransaction(Transaction transaction) throws IOException {
        URI transactionsURI;
        try {
            transactionsURI = new URIBuilder(serverURI).setPath("/transactions").build();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        HttpPost submitTransactionRequest = new HttpPost(transactionsURI);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tx", transaction.toEnvelopeXdrBase64()));
        submitTransactionRequest.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = httpClient.execute(submitTransactionRequest);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream responseStream = entity.getContent();
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(responseStream, writer, StandardCharsets.UTF_8);
                String responseString = writer.toString();
                SubmitTransactionResponse submitTransactionResponse = GsonSingleton.getInstance().fromJson(responseString, SubmitTransactionResponse.class);
                return submitTransactionResponse;
            } finally {
                responseStream.close();
            }
        }
        return null;
    }

    /**
     * To support mocking a client
     *
     * @param httpClient
     */
    void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
