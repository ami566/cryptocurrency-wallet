package bg.sofia.uni.fmi.mjt.cryptowallet.assets;

import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.HttpException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.NoSuchAssetException;
import bg.sofia.uni.fmi.mjt.cryptowallet.restServer.ApiResponse;
import bg.sofia.uni.fmi.mjt.cryptowallet.restServer.ServerRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AssetsDatabaseTest {

    @Mock
    private ServerRequest assetsInApi = mock(ServerRequest.class);

    @InjectMocks
    private AssetsDatabase assetsDb = new AssetsDatabase(assetsInApi);

    @Test
    public void testGetAssetByIdWithEmptyIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetsDb.getAssetById(""));
    }

    @Test
    public void testGetAssetByIdWithNullIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> assetsDb.getAssetById(null));
    }

    @Test
    public void testGetAssetByIdReturnsCorrectAsset() throws NoSuchAssetException, HttpException, URISyntaxException {
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        ApiResponse<Asset> apiResponse = new ApiResponse<>(
                testAsset,
                HTTP_OK, null
        );
        when(assetsInApi.getAssetById(anyString())).thenReturn(
                CompletableFuture.completedFuture(apiResponse)
        );

        assertEquals(testAsset, assetsDb.getAssetById("test"),
                "Incorrect asset returned from database");
    }

    @Test
    public void testGetAssetByOfferingCodeWithNonExistingOfferingCodeThrowsException() throws URISyntaxException {
        ApiResponse<Asset> apiResponse = new ApiResponse<>(
                null,
                HTTP_OK, null
        );
        when(assetsInApi.getAssetById(anyString())).thenReturn(
                CompletableFuture.completedFuture(apiResponse)
        );

        assertThrows(NoSuchAssetException.class, () -> assetsDb.getAssetById("test"),
                "Response with null data should have resulted in NoSuchAssetException");
    }

    @Test
    public void testGetAssetByIdThrowsHttpException() throws URISyntaxException {
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        ApiResponse<Asset> apiResponse = new ApiResponse<>(
                testAsset,
                HTTP_BAD_REQUEST, null
        );
        when(assetsInApi.getAssetById(anyString())).thenReturn(
                CompletableFuture.completedFuture(apiResponse)
        );

        assertThrows(HttpException.class, () -> assetsDb.getAssetById("test"),
                "Response with status code different from 200 should result in exception");
    }

    @Test
    public void testGetAssetsFromApiThrowsHttpException() throws URISyntaxException {
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        ApiResponse<List<Asset>> apiResponse = new ApiResponse<>(
                List.of(testAsset),
                HTTP_BAD_REQUEST, null
        );

        when(assetsInApi.getAssets()).thenReturn(
                CompletableFuture.completedFuture(apiResponse)
        );

        assertThrows(HttpException.class, () -> assetsDb.getAllAssets(),
                "Response with status code different 200 should result in exception");
    }

    @Test
    public void testGetAssetsFromApiCorrect() throws URISyntaxException, HttpException {
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        List<Asset> listData = List.of(testAsset);

        ApiResponse<List<Asset>> apiResponse = new ApiResponse<>(
                listData,
                HTTP_OK, null
        );

        when(assetsInApi.getAssets()).thenReturn(
                CompletableFuture.completedFuture(apiResponse)
        );

        assertIterableEquals(listData, assetsDb.getAllAssets().values(), "Invalid list result");
    }
}
