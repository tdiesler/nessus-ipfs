package io.nessus.testing;

/*-
 * #%L
 * Nessus :: API
 * %%
 * Copyright (C) 2018 Nessus
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.nessus.AbstractWallet;
import io.nessus.Blockchain;
import io.nessus.BlockchainFactory;
import io.nessus.Config;
import io.nessus.Network;
import io.nessus.UTXO;
import io.nessus.Wallet;

public abstract class AbstractBlockchainTest {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public static final String LABEL_BOB = "Bob";
    public static final String LABEL_MARY = "Mary";
    public static final String LABEL_LUI = "Lui";
    public static final String LABEL_SINK = "Sink";
    
    public static final String ADDRESS_BOB = "n3ha6rJa8ZS7B4v4vwNWn8CnLHfUYXW1XE";
    public static final String ADDRESS_MARY = "mm2PoHeFncAStYeZJSSTa4bmUVXRa3L6PL";
    public static final String ADDRESS_LUI = "mosjJrQTL7HdsaXNSjRZ52y47WygLECxYQ";
    
    protected static void importAddresses(Wallet wallet, Class<?> configSource) throws IOException {
        
        URL configURL = configSource.getResource("/initial-import.json");
        if (configURL != null) {
            Config config = Config.parseConfig(configURL);
            wallet.importAddresses(config);
        }
    }
    
    protected static void generate(Blockchain blockchain) {
        Wallet wallet = blockchain.getWallet();
        Network network = blockchain.getNetwork();
        BigDecimal balance = wallet.getBalance("");
        if (balance.doubleValue() == 0.0) {
            List<String> blocks = network.generate(101, null);
            Assert.assertEquals(101, blocks.size());
        }
    }
    
    protected static BigDecimal getUTXOAmount(List<UTXO> utxos) {
        return AbstractWallet.getUTXOAmount(utxos);
    }
    
    protected void showAccountBalances() {
        Blockchain blockchain = BlockchainFactory.getBlockchain();
        Wallet wallet = blockchain.getWallet();
        for (String label : wallet.getLabels()) {
            if (!label.equals(Wallet.LABEL_CHANGE)) {
                BigDecimal val = wallet.getBalance(label);
                LOG.info(String.format("%-5s: %13.8f", label, val));
            }
        }
    }
}
