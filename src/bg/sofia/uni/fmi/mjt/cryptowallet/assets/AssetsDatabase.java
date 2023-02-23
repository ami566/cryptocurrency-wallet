package bg.sofia.uni.fmi.mjt.cryptowallet.assets;

import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;
import bg.sofia.uni.fmi.mjt.cryptowallet.restServer.ApiResponse;
import bg.sofia.uni.fmi.mjt.cryptowallet.restServer.ServerRequest;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class AssetsDatabase {
    private static final int MINUTES_TO_UPDATE = 30;
    private static final int CAPACITY = 100;
    private static final int HTTP_TOO_MANY_REQUESTS_CODE = 429;
    private Map<String, Asset> assets;
    private LocalDateTime timeOfLastUpdate;
    private ServerRequest assetsInApi;

    public AssetsDatabase(ServerRequest request) {
        assetsInApi = request;
        assets = new HashMap<>(CAPACITY);
    }

    public Map<String, Asset> getAllAssets() throws HttpException, URISyntaxException {
        if (assets.size() != CAPACITY) {
            assets.clear();
            timeOfLastUpdate = LocalDateTime.now();

            List<Asset> assetsFromApi = getAssetsFromApi();
            for (Asset asset : assetsFromApi) {
                assets.put(asset.assetId(), asset);
            }
        }

        if (isCachedDataOld()) {
            updateDatabase();
        }

        return Collections.unmodifiableMap(assets);
    }

    public Asset getAssetById(String id) throws NoSuchAssetException, HttpException, URISyntaxException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Id cannot be empty or null");
        }

        if (isCachedDataOld()) {
            updateDatabase();
        }
        if (!assets.containsKey(id)) {
            Asset asset = getAssetFromApiById(id);
            putAssetInDatabase(asset);
        }
        return assets.get(id);
    }

    private void putAssetInDatabase(Asset a) {
        Collection<Asset> list = assets.values();
        if (list.size() == CAPACITY) {
            Asset asset = list.stream().findFirst().orElse(null);
            assets.remove(asset.assetId());
        }

        assets.put(a.assetId(), a);
    }

    private List<Asset> getAssetsFromApi() throws URISyntaxException, HttpException {
        ApiResponse<List<Asset>> response = assetsInApi.getAssets().join();
        if (response.getStatusCode() != HTTP_OK) {
            throwHttpException(response.getStatusCode(), response.getMessage());
        }

        return response.getData()
                .stream()
                .limit(CAPACITY)
                .toList();
    }

    private Asset getAssetFromApiById(String id)
            throws URISyntaxException, HttpException, NoSuchAssetException {
        ApiResponse<Asset> assetResponse = assetsInApi.getAssetById(id).join();
        if (assetResponse.getData() == null) {
            throw new NoSuchAssetException("Crypto with this code does not exist");
        }

        return getAssetDataFromResponse(assetResponse);
    }

    private Asset getAssetDataFromResponse(ApiResponse<Asset> assetResponse) throws HttpException {
        if (assetResponse.getStatusCode() != HTTP_OK) {
            throwHttpException(assetResponse.getStatusCode(), assetResponse.getMessage());
        }

        return assetResponse.getData();
    }

    private void throwHttpException(int statusCode, String message)
            throws HttpException {
        switch (statusCode) {
            case HTTP_BAD_REQUEST -> throw new BadRequestException(message);
            case HTTP_UNAUTHORIZED -> throw new UnauthorizedException(message);
            case HTTP_FORBIDDEN -> throw new ForbiddenException(message);
            case HTTP_TOO_MANY_REQUESTS_CODE -> throw new TooManyRequestsException(message);
        }
    }

    private boolean isCachedDataOld() {
        if (timeOfLastUpdate == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        long differenceMin = timeOfLastUpdate.until(now, ChronoUnit.MINUTES);
        return differenceMin >= MINUTES_TO_UPDATE;
    }

    private void updateDatabase() throws URISyntaxException, HttpException {
        timeOfLastUpdate = LocalDateTime.now();

        List<CompletableFuture<ApiResponse<Asset>>> list = new ArrayList<>();

        for (Map.Entry<String, Asset> assetEntry : assets.entrySet()) {
            list.add(assetsInApi.getAssetById(assetEntry.getKey()));
        }

        assets.clear();
        List<ApiResponse<Asset>> apiResponses = list.stream().map(CompletableFuture::join).toList();

        for (ApiResponse<Asset> apiResponse : apiResponses) {
            Asset asset = getAssetDataFromResponse(apiResponse);
            assets.put(asset.assetId(), asset);
        }
    }
}
