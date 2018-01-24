package org.stellar.sdk.requests;

import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.responses.GsonSingleton;
import org.stellar.sdk.responses.TradeResponse;

import java.io.IOException;
import java.net.URI;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Builds requests connected to trades.
 */
public class TradesRequestBuilder extends RequestBuilder {

    public TradesRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "order_book/trades");
    }

    public TradesRequestBuilder buyingAsset(Asset asset) {
        urlBuilder.addQueryParameter("buying_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            urlBuilder.addQueryParameter("buying_asset_code", creditAlphaNumAsset.getCode());
            urlBuilder.addQueryParameter("buying_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    public TradesRequestBuilder sellingAsset(Asset asset) {
        urlBuilder.addQueryParameter("selling_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            urlBuilder.addQueryParameter("selling_asset_code", creditAlphaNumAsset.getCode());
            urlBuilder.addQueryParameter("selling_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    public TradeResponse execute(URI uri) throws IOException, TooManyRequestsException {
        Response response = httpClient.newCall(new Request.Builder().url(uri.toString()).build()).execute();
        return GsonSingleton.getInstance().fromJson(response.body().toString(), TradeResponse.class);
    }

    public TradeResponse execute() throws IOException, TooManyRequestsException {
        return this.execute(this.buildUri());
    }
}
