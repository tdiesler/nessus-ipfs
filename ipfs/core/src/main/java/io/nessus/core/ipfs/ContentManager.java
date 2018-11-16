package io.nessus.core.ipfs;

/*-
 * #%L
 * Nessus :: IPFS
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
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

import io.nessus.Blockchain;
import io.nessus.Wallet.Address;

public interface ContentManager {

    /**
     * Get the Blockchain
     */
    Blockchain getBlockchain();
    
    /**
     * Get the IPFS client
     */
    IPFSClient getIPFSClient();
    
    /**
     * Register an address with the system.
     * 
     * @return An Eliptic Curve public key
     */
    PublicKey register(Address addr) throws GeneralSecurityException;
    
    /**
     * Find the registered public key for a given address.
     * 
     * @return An Eliptic Curve public key
     */
    PublicKey findRegistation(Address addr);
    
    /**
     * Add content to IPFS.
     */
    FHandle add(Address owner, InputStream input, Path path) throws IOException, GeneralSecurityException;
    
    /**
     * Get content from IPFS. 
     */
    FHandle get(Address owner, String cid, Path path, Long timeout) throws IOException, GeneralSecurityException;
    
    /**
     * Send content to a target address via IPFS. 
     */
    FHandle send(Address owner, String cid, Address target, Long timeout) throws IOException, GeneralSecurityException;

    /**
     * Find registered IPFS content for a given address.
     */
    List<FHandle> findIPFSContent(Address owner, Long timeout) throws IOException;
    
    /**
     * Find local content for a given address.
     */
    List<FHandle> findLocalContent(Address owner) throws IOException;
    
    /**
     * Show content of a plain file from local storage.  
     */
    InputStream getLocalContent(Address owner, Path path) throws IOException;
    
    /**
     * Delete a plain file from local storage.  
     */
    boolean deleteLocalContent(Address owner, Path path) throws IOException;
}