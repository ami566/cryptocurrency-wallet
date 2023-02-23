package bg.sofia.uni.fmi.mjt.cryptowallet.assets;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Asset implements Serializable {
    @SerializedName("asset_id")
    private String assetId;

    private String name;

    @SerializedName("type_is_crypto")
    private int typeIsCrypto;

    @SerializedName("price_usd")
    private double priceUsd;

    @SerializedName("data_start")
    private String dataStart;

    @SerializedName("data_end")
    private String dataEnd;

    public Asset(
            String assetId,
            String name,
            int typeIsCrypto,
            double priceUsd,
            String dataStart,
            String dataEnd
    ) {
        this.assetId = assetId;
        this.name = name;
        this.typeIsCrypto = typeIsCrypto;
        this.priceUsd = priceUsd;
        this.dataStart = dataStart;
        this.dataEnd = dataEnd;
    }

    public String assetId() {
        return assetId;
    }

    public String name() {
        return name;
    }

    public int typeIsCrypto() {
        return typeIsCrypto;
    }

    public double priceUsd() {
        return priceUsd;
    }

    public String dataStart() {
        return dataStart;
    }

    public String dataEnd() {
        return dataEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset)) return false;
        Asset asset = (Asset) o;
        return assetId.equals(asset.assetId);
    }

    @Override
    public String toString() {
        return String.format("""
                Asset {
                    assetId: '%s',
                    name: '%s',
                    priceUsd: '%f',
                    dataStart: '%s',
                    dataEnd: '%s'
                }
                """, assetId, name, priceUsd, dataStart, dataEnd);
    }

}