package io.nessus.cipher;

/*-
 * #%L
 * Nessus :: Cipher
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.nessus.utils.AssertArgument;
import io.nessus.utils.AssertState;
import io.nessus.utils.StreamUtils;

public class AESCipher {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate the secret key from a random source.
     */
    public SecretKey getSecretKey() {

        // Use a randomly generated 128 bit key.
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Encode the given secret key
     */
    public String encodeSecretKey(SecretKey secKey) {
        byte[] raw = secKey.getEncoded();
        return Base64.getEncoder().encodeToString(raw);
    }

    /**
     * Decode the given secret key
     */
    public SecretKey decodeSecretKey(String encodedForm) {

        byte[] raw = Base64.getDecoder().decode(encodedForm);
        AssertArgument.assertTrue(raw.length == 16, "Expected 128 bit");
        return new SecretKeySpec(raw, "AES");
    }

    public InputStream encrypt(SecretKey secKey, InputStream ins) throws IOException, GeneralSecurityException {
        return encrypt(secKey, null, ins);
    }
    
    public InputStream encrypt(SecretKey secKey, byte[] addData, InputStream ins) throws IOException, GeneralSecurityException {

        // Then we have to create our initialization vector (IV). 
        // For GCM a 12 byte random byte-array is recommend by NIST because it’s faster and more secure. 
        // Be mindful to always use a strong pseudorandom number generator (PRNG) like SecureRandom.

        byte[] iv = new byte[12]; //NEVER REUSE THIS IV WITH SAME KEY
        secureRandom.nextBytes(iv);

        // Then initialize your cipher and add the optional associated data

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secKey, parameterSpec);
        if (addData != null) { 
            cipher.updateAAD(addData);
        }

        // Encrypt

        InputStream cis = new CipherInputStream(ins, cipher);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamUtils.copyStream(cis, baos);
        byte[] cipherText = baos.toByteArray();

        // Now concat all of it to a single message

        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        byte[] result = byteBuffer.array();

        return new ByteArrayInputStream(result);
    }

    public InputStream decrypt(SecretKey secKey, InputStream secretStream) throws IOException, GeneralSecurityException {
        return decrypt(secKey, null, secretStream);
    }
    
    public InputStream decrypt(SecretKey secKey, byte[] addData, InputStream secretStream) throws IOException, GeneralSecurityException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamUtils.copyStream(secretStream, baos);
        byte[] secretMessage = baos.toByteArray();
        
        AssertArgument.assertTrue(secretMessage.length > 0, "Secret message cannot be empty");
        
        // First deconstruct the message

        ByteBuffer byteBuffer = ByteBuffer.wrap(secretMessage);
        int ivLength = byteBuffer.getInt();
        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        // Initialize the cipher and add the optional associated data and decrypt

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secKey, new GCMParameterSpec(128, iv));
        if (addData != null) { 
            cipher.updateAAD(addData);
        }

        // Decrypt

        InputStream ins = new ByteArrayInputStream(cipherText);
        InputStream cis = new CipherInputStream(ins, cipher);
        baos = new ByteArrayOutputStream();
        StreamUtils.copyStream(cis, baos);
        byte[] result = baos.toByteArray();

        AssertState.assertTrue(result.length > 0, "Decryption results in empty message");
        
        return new ByteArrayInputStream(result);
    }

}
