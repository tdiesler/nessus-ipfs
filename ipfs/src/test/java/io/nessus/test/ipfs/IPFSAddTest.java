package io.nessus.test.ipfs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.nessus.Wallet.Address;
import io.nessus.ipfs.FHandle;

public class IPFSAddTest extends AbstractWorkflowTest {

    @Before
    public void before() throws Exception {
        super.before();
        
        Address addrBob = wallet.getAddress(LABEL_BOB);
        PublicKey pubKey = cntmgr.findRegistation(addrBob);
        
        if (pubKey == null) {
            cntmgr.register(addrBob);
            pubKey = cntmgr.findRegistation(addrBob);
        }
    }
    
    @Test
    public void findNonExisting() throws Exception {

        String content = "some text";
        InputStream input = new ByteArrayInputStream(content.getBytes());
        
        Path path = Paths.get("my/src/path");
        FHandle fhandle = cntmgr.add(addrBob, input, path);
        
        // Unnecessary delay when adding encrypted content
        // https://github.com/tdiesler/nessus/issues/41
        
        // Use an extremely short timeout 
        FHandle fhres = findIPFSContent(addrBob, fhandle.getCid(), 10L);
        Assert.assertTrue(fhres.isAvailable());
    }
}