package io.nessus.ipfs.jaxrs;

/*-
 * #%L
 * Nessus :: IPFS :: JAXRS
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
import java.net.URL;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ipfs.multihash.Multihash;
import io.nessus.Network;
import io.nessus.Wallet;
import io.nessus.Wallet.Address;
import io.nessus.cipher.utils.RSAUtils;
import io.nessus.ipfs.AHandle;
import io.nessus.ipfs.ContentManager;
import io.nessus.ipfs.FHandle;
import io.nessus.utils.AssertArgument;
import io.nessus.utils.AssertState;

public class JAXRSResource implements JAXRSEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(JAXRSResource.class);

    private final ContentManager cntmgr;
    
    public JAXRSResource() throws IOException {

        // [TODO] Inject this propperly
        cntmgr = JAXRSApplication.INSTANCE.cntManager;
    }

    @Override
    public SAHandle registerAddress(String addr) throws GeneralSecurityException, IOException {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        AHandle ahandle = cntmgr.registerAddress(owner);
        
        PublicKey pubKey = ahandle.getPubKey();
		SAHandle shandle = createAddrHandle(owner, pubKey);
        LOG.info("/regaddr {} => {}", addr, shandle);

        return shandle;
    }

    @Override
    public List<SAHandle> findAddressInfo(String label, String addr) {

        assertBlockchainNetworkAvailable();
        
        Address owner = addr != null ? assertWalletAddress(addr) : null;
        
        Wallet wallet = cntmgr.getBlockchain().getWallet();
        List<SAHandle> shandles = wallet.getAddresses().stream()
                .filter(a -> !a.getLabels().contains(Wallet.LABEL_CHANGE))
                .filter(a -> owner == null || a.equals(owner))
                .filter(a -> label == null || a.getLabels().contains(label))
                .map(a -> {
                    AHandle ahandle = cntmgr.findAddressRegistation(a, null);
                    PublicKey pubKey = ahandle != null ? ahandle.getPubKey() : null;
                    SAHandle shandle = createAddrHandle(a, pubKey);
                    return shandle;
                })
                .collect(Collectors.toList());

        if (shandles.size() < 2)
        	LOG.info("/addrinfo {} {} => {}", label, addr, shandles);
        else 
        	LOG.info("/addrinfo {} {} => {} addrs", label, addr, shandles.size());

        return shandles;
    }

    @Override
    public SAHandle unregisterAddress(String addr) {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        AHandle ahandle = cntmgr.unregisterAddress(owner);

        PublicKey pubKey = ahandle != null ? ahandle.getPubKey() : null;
        SAHandle shandle = createAddrHandle(owner, pubKey);
        LOG.info("/rmaddr {} => {}", addr, shandle);

        return shandle;
    }

    @Override
    public SFHandle addIpfsContent(String addr, String path, URL srcUrl) throws IOException, GeneralSecurityException {
        AssertArgument.assertTrue(path != null || srcUrl != null, "Path or URL must be given");

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        FHandle fhandle;
        if (srcUrl != null) {
            fhandle = cntmgr.addIpfsContent(owner, Paths.get(path), srcUrl);
        } else {
            fhandle = cntmgr.addIpfsContent(owner, Paths.get(path));
        }

        SFHandle shandle = new SFHandle(fhandle);
        LOG.info("/addipfs {}", shandle);

        return shandle;
    }

    @Override
    public SFHandle addIpfsContent(String addr, String path, InputStream input) throws IOException, GeneralSecurityException {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        FHandle fhandle = cntmgr.addIpfsContent(owner, Paths.get(path), input);

        SFHandle shandle = new SFHandle(fhandle);
        LOG.info("/addipfs {}", shandle);

        return shandle;
    }

    @Override
    public SFHandle getIpfsContent(String addr, String cid, String path, Long timeout) throws IOException, GeneralSecurityException {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        FHandle fhandle = cntmgr.getIpfsContent(owner, Multihash.fromBase58(cid), Paths.get(path), timeout);

        SFHandle shandle = new SFHandle(fhandle);
        LOG.info("/getipfs {} => {}", cid, shandle);

        return shandle;
    }

    @Override
    public SFHandle sendIpfsContent(String addr, String cid, @QueryParam("target") String rawTarget, Long timeout) throws IOException, GeneralSecurityException {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        Address target = assertWalletAddress(rawTarget);

        FHandle fhandle = cntmgr.sendIpfsContent(owner, Multihash.fromBase58(cid), target, timeout);

        SFHandle shandle = new SFHandle(fhandle);
        LOG.info("/sendipfs {} => {}", cid, shandle);

        return shandle;
    }

    @Override
    public List<SFHandle> findIpfsContent(String addr, Long timeout) throws IOException {

        assertBlockchainNetworkAvailable();
        
        List<SFHandle> shandles = new ArrayList<>();

        Address owner = assertWalletAddress(addr);
        for (FHandle fh : cntmgr.findIpfsContent(owner, timeout)) {
            shandles.add(new SFHandle(fh));
        }
        
        if (shandles.size() < 2)
            LOG.info("/findipfs {} => {}", addr, shandles);
        else 
            LOG.info("/findipfs {} => {} files", addr, shandles.size());

        return shandles;
    }

    @Override
    public List<String> unregisterIpfsContent(String addr, List<String> rawids) throws IOException {
        
        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        
        List<Multihash> cids = rawids.stream().map(id -> Multihash.fromBase58(id)).collect(Collectors.toList());
        
        List<Multihash> result = cntmgr.unregisterIpfsContent(owner, cids);
        
        LOG.info("/rmipfs {} {} => {}", addr, cids, result);

        return result.stream().map(cid -> cid.toBase58()).collect(Collectors.toList());
    }

    @Override
    public List<SFHandle> findLocalContent(String addr, String path) throws IOException {

        assertBlockchainNetworkAvailable();
        
        List<SFHandle> shandles = new ArrayList<>();

        Address owner = assertWalletAddress(addr);
        if (path == null) {
            for (FHandle fhres : cntmgr.findLocalContent(owner)) {
                shandles.add(new SFHandle(fhres));
            }
        } else {
            FHandle fhres = cntmgr.findLocalContent(owner, Paths.get(path));
            if (fhres != null) shandles.add(new SFHandle(fhres));
        }

        if (shandles.size() < 2)
            LOG.info("/findlocal {} {} => {}", addr, path, shandles);
        else 
            LOG.info("/findlocal {} {} => {} files", addr, path, shandles.size());
        
        return shandles;
    }

    @Override
    public InputStream getLocalContent(String addr, String path) throws IOException {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        InputStream content = cntmgr.getLocalContent(owner, Paths.get(path));
        
        LOG.info("/getlocal {} {}", addr, path);

        return content;
    }

    @Override
    public boolean removeLocalContent(String addr, String path) throws IOException {

        assertBlockchainNetworkAvailable();
        
        Address owner = assertWalletAddress(addr);
        boolean removed = cntmgr.removeLocalContent(owner, Paths.get(path));
        
        LOG.info("/rmlocal {} {} => {}", addr, path, removed);

        return removed;
    }

    private void assertBlockchainNetworkAvailable() {
        Network network = cntmgr.getBlockchain().getNetwork();
        JAXRSClient.assertBlockchainNetworkAvailable(network);
    }

    private Address assertWalletAddress(String rawaddr) {
        Address addr = cntmgr.getBlockchain().getWallet().findAddress(rawaddr);
        AssertState.assertNotNull(addr, "Unknown address: " + addr);
        return addr;
    }
    
    private SAHandle createAddrHandle(Address owner, PublicKey pubKey) {
        Wallet wallet = cntmgr.getBlockchain().getWallet();
        String encKey = pubKey != null ? RSAUtils.encodeKey(pubKey) : null;
        return new SAHandle(owner, encKey, wallet.getBalance(owner));
    }
}
