package io.nessus.bitcoin;

/*-
 * #%L
 * Nessus :: Bitcoin
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

import java.math.BigDecimal;

import io.nessus.AbstractNetwork;
import io.nessus.Block;
import io.nessus.Blockchain;
import io.nessus.Network;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

public class BitcoinNetwork extends AbstractNetwork implements Network {

    public BitcoinNetwork(Blockchain blockchain, BitcoindRpcClient client) {
        super(blockchain, client);
    }

    @Override
    public String getVersion() {
        return "" + client.getNetworkInfo().version();
    }

    @Override
    public BigDecimal estimateSmartFee(Integer blocks) {
        blocks = blocks != null ? blocks : 2;
        
        // https://en.bitcoin.it/wiki/Transaction_fees#Settings
        BigDecimal feeRate = client.estimateSmartFee(blocks).feeRate();
        feeRate = feeRate != null ? feeRate : new BigDecimal("0.00004000");
        
        return feeRate;
    }

    @Override
    public BigDecimal getMinTxFee() {
        // min relay fee not met, 300 < 1073 (code 66)
        return new BigDecimal("0.00001500");
    }

    @Override
    public Integer getBlockRate() {
        return 600;
    }

    @Override
    public BigDecimal getDustThreshold() {
    	// 546 satoshis at the default rate of 3000 sat/kB.
        // https://github.com/bitcoin/bitcoin/blob/master/src/policy/policy.cpp#L18
        return new BigDecimal("0.00000546");
    }

    @Override
    public BigDecimal getMinDataAmount() {
        return BigDecimal.ZERO;
    }

    @Override
    public Block getBlock(String blockHash) {
        return new BitcoinBlock(client.getBlock(blockHash));
    }
}
