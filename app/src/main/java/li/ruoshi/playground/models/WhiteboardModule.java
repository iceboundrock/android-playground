package li.ruoshi.playground.models;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ruoshili on 6/24/15.
 */

public class WhiteboardModule {
    public Config provideConfig() {
        return new Config();
    }



}
