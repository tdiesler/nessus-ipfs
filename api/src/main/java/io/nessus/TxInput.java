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

public class TxInput {
    
    private final String txId;
    private final Integer vout;
    private final String scriptPubKey;
    
    public TxInput(String txId, Integer vout, String scriptPubKey) {
        this.txId = txId;
        this.vout = vout;
        this.scriptPubKey = scriptPubKey;
    }

    public String getTxId() {
        return txId;
    }

    public Integer getVout() {
        return vout;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + txId.hashCode();
        result = 31 * result + vout.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TxInput)) return false;
        TxInput other = (TxInput) obj;
        return txId.equals(other.txId) && vout.equals(other.vout);
    }

    public String toString() {
        return String.format("[txid=%s, vout=%d, spk=%s]", txId, vout, scriptPubKey);
    }
}
