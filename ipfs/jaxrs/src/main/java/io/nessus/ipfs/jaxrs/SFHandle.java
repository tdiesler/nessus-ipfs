package io.nessus.ipfs.jaxrs;

import java.io.PrintWriter;
import java.io.StringWriter;

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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.nessus.ipfs.CidPath;
import io.nessus.ipfs.FHandle;
import io.nessus.utils.AssertArgument;

public class SFHandle {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String cid;
    private String owner;
    private String path;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String txId;
    private boolean encrypted;
    private boolean available;
    
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean expired;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int attempt;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private long elapsed;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private List<SFHandle> children = new ArrayList<>();

    public SFHandle() {
    }

    public SFHandle(String owner, String cid, String path, boolean available, boolean encrypted) {
    	AssertArgument.assertNotNull(owner, "Null owner");
    	AssertArgument.assertNotNull(path, "Null path");
        this.cid = cid;
        this.path = path;
        this.owner = owner;
        this.available = available;
        this.encrypted = encrypted;
    }

    public SFHandle(FHandle fhandle) {
    	AssertArgument.assertNotNull(fhandle, "Null fhandle");
    	AssertArgument.assertNotNull(fhandle.getOwner(), "Null owner");
    	AssertArgument.assertNotNull(fhandle.getPath(), "Null path");
        Path path = fhandle.getPath();
		CidPath cid = fhandle.getCidPath();
		this.cid = cid != null ? cid.toString() : null;
        this.owner = fhandle.getOwner().getAddress();
        this.path = path != null ? path.toString() : null;
        this.txId = fhandle.getTxId();
        this.encrypted = fhandle.isEncrypted();
        this.available = fhandle.isAvailable();
        this.expired = fhandle.isExpired();
        this.attempt = fhandle.getAttempt();
        this.elapsed = fhandle.getElapsed();
        fhandle.getChildren().forEach(ch -> addChild(new SFHandle(ch)));
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public Long getElapsed() {
        return elapsed;
    }

    public void setElapsed(Long elapsed) {
        this.elapsed = elapsed;
    }

    private boolean hasChildren() {
        return children.size() > 0;
    }

    public List<SFHandle> getChildren() {
        return children;
    }

    public void setChildren(List<SFHandle> children) {
        this.children = children;
    }

    private void addChild(SFHandle child) {
        children.add(child);
    }

    public String toString(boolean verbose) {
        if (!verbose) return toString();
        StringWriter sw = new StringWriter();
        recursiveString(new PrintWriter(sw), 0, this);
        return sw.toString().trim();
    }

    private void recursiveString(PrintWriter pw, int indent, SFHandle fh) {
        char[] pad = new char[indent];
        Arrays.fill(pad, ' ');
        pw.println(new String(pad) + fh);
        if (fh.hasChildren()) {
            fh.children.forEach(ch -> recursiveString(pw, indent + 3, ch));
        }
    }

    public String toString() {
    	return String.format("[addr=%s, cid=%s, path=%s, avl=%d, exp=%d, try=%d, time=%s]", 
    			owner, cid, path, available ? 1 : 0, expired ? 1 : 0, attempt, elapsed);
    }
}
