package org.stellar.sdk.requests;

import com.google.gson.reflect.TypeToken;

import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.PathResponse;

import java.io.IOException;
import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Builds requests connected to paths.
 */
public class PathsRequestBuilder extends RequestBuilder {

    public PathsRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "paths");
    }

    public PathsRequestBuilder destinationAccount(KeyPair account) {
        urlBuilder.addQueryParameter("destination_account", account.getAccountId());
        return this;
    }

    public PathsRequestBuilder sourceAccount(KeyPair account) {
        urlBuilder.addQueryParameter("source_account", account.getAccountId());
        return this;
    }

    public PathsRequestBuilder destinationAmount(String amount) {
        urlBuilder.addQueryParameter("destination_amount", amount);
        return this;
    }

    public PathsRequestBuilder destinationAsset(Asset asset) {
        urlBuilder.addQueryParameter("destination_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            urlBuilder.addQueryParameter("destination_asset_code", creditAlphaNumAsset.getCode());
            urlBuilder.addQueryParameter("destination_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    /**
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<PathResponse> execute(URI uri) throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<Page<PathResponse>>() {};
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().string(), type.getType());
    }

    /**
     * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
     * @throws IOException
     */
    public Page<PathResponse> execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }
}
