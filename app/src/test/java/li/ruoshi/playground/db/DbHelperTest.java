package li.ruoshi.playground.db;


import android.text.TextUtils;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import li.ruoshi.playground.BuildConfig;

/**
 * Created by ruoshili on 6/11/15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 18)
public class DbHelperTest {


    @org.junit.Test
    public void testOnCreate() throws Exception {
        Assert.assertTrue(TextUtils.isEmpty(""));
    }


}