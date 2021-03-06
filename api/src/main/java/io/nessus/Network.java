package io.nessus;

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

import java.math.BigDecimal;
import java.util.List;

import io.nessus.Wallet.Address;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.NetworkInfo;

public interface Network {

    /**
     * Get the network version
     */
    String getVersion();
    
    /**
     * Estimates the transaction fee per kilobyte that needs to be paid 
     * for a transaction to be included within a certain number of blocks.
     */
    BigDecimal estimateSmartFee(Integer blocks);

    /**
     * Get the minimum transaction fee
     */
    BigDecimal getMinTxFee();
    
    /**
     * "Dust" is defined in terms of dustRelayFee, which has units satoshis-per-kilobyte.
     * If you'd pay more in fees than the value of the output, then it is considered dust.
     */
    BigDecimal getDustThreshold();

    /**
     * The minimum amout that must be given to an OP_RETURN output. 
     */
    BigDecimal getMinDataAmount();
    
    /**
     * Get the block for the given hash
     */
    Block getBlock(String blockHash);

    /**
     * Get the block hash for the given height
     */
    String getBlockHash(Integer height);

    /**
     * Get the block count
     */
    Integer getBlockCount();

    /**
     * Get the block rate in seconds
     */
    Integer getBlockRate();

    /**
     * Returns information about the node’s connection to the network.
     */
    NetworkInfo getNetworkInfo();
    
    /**
     * Generate the given number of blocks to the given address
     */
    List<String> generate(int numBlocks, Address addr);
}
