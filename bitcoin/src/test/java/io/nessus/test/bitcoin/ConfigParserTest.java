package io.nessus.test.bitcoin;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.nessus.test.bitcoin.dto.Config;

public class ConfigParserTest {

    final Logger LOG = LoggerFactory.getLogger(ConfigParserTest.class);

    @Test
    public void testSimple() throws Exception {

        Config config = Config.parseConfig("/initial-import.json");
        LOG.info("{}", config);
        
        Assert.assertEquals(3, config.getWallet().getAddresses().size());
    }
}
