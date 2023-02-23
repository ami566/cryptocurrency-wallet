package bg.sofia.uni.fmi.mjt.cryptowallet.restServer;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.net.HttpURLConnection.HTTP_OK;

public class ServerRequest {
    private static final String QUERY_URL = "https://rest.coinapi.io/v1/assets";
    private static final String API_KEY = "ED9C087D-9E40-4335-B3A3-717481597830";
    private static final String API_KEY_NAME = "X-CoinAPI-Key";
    private final Gson gson;
    private final HttpClient client;

    public ServerRequest() {
        client = HttpClient.newBuilder().build();
        gson = new Gson();
    }

    public CompletableFuture<ApiResponse<List<Asset>>> getAssets() throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder(new URI(QUERY_URL))
                .GET()
                .header(API_KEY_NAME, API_KEY)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::responseToStatusCodeAssets);
    }

    public CompletableFuture<ApiResponse<Asset>> getAssetById(String assetId) throws URISyntaxException {
        String url = String.format(QUERY_URL + "/" + assetId);

        HttpRequest request = HttpRequest.newBuilder(new URI(url))
                .GET()
                .header(API_KEY_NAME, API_KEY)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::responseToStatusCodeAsset);
    }

    private ApiResponse<Asset> responseToStatusCodeAsset(HttpResponse<String> response) {
        if (response.statusCode() != HTTP_OK) {
            String message = gson.fromJson(response.body(), ResponseError.class).getMessage();
            return new ApiResponse<>(null, response.statusCode(), message);
        }

        Type assetsListType = new TypeToken<ArrayList<Asset>>() {
        }.getType();
        List<Asset> list = gson.fromJson(response.body(), assetsListType);
        Asset assetDto = getCryptoCurrencies(list).stream().findFirst().orElse(null);
        return new ApiResponse<>(assetDto, response.statusCode(), null);
    }

    private ApiResponse<List<Asset>> responseToStatusCodeAssets(HttpResponse<String> response) {
        if (response.statusCode() != HTTP_OK) {
            String message = gson.fromJson(response.body(), ResponseError.class).getMessage();
            return new ApiResponse<>(null, response.statusCode(), message);
        }

        Type assetsListType = new TypeToken<ArrayList<Asset>>() {
        }.getType();
        List<Asset> list = gson.fromJson(response.body(), assetsListType);
        return new ApiResponse<>(getCryptoCurrencies(list),
                response.statusCode(), null);
    }

    private List<Asset> getCryptoCurrencies(List<Asset> assets) {
        return assets.stream()
                .filter(el -> el.typeIsCrypto() == 1 && el.priceUsd() > 0)
                .toList();
    }
}
