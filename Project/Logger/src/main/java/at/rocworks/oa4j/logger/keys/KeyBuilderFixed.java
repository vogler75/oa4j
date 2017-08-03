package at.rocworks.oa4j.logger.keys;

import at.rocworks.oa4j.logger.data.Dp;

/**
 * Created by vogler on 8/3/2017.
 */
public class KeyBuilderFixed extends KeyBuilder {
    private final String store;
    public KeyBuilderFixed(String store) {
        super();
        this.store=store;
    }

    @Override
    public String getStoreOfDp(Dp dp) {
        return store;
    }
}
