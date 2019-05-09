package com.mildmelon.noobcoin.app.utils;

import com.mildmelon.noobcoin.app.Transaction;

import java.nio.charset.StandardCharsets;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil
{

    // Applies Sha256 to a string and returns the result
    public static String applySha256(String input)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8)); // Applies sha256 to our input

            StringBuilder hexString = new StringBuilder(); // This will contain our hash as a hexadecimal
            for (int i = 0; i < hash.length; i++)
            {
                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1)
                {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // Applies ECDSA Signature and returns the result (as bytes)
    public static byte[] applySignature(PrivateKey privateKey, String input)
    {
        byte[] output;

        try
        {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            byte[] strByte = input.getBytes();

            dsa.initSign(privateKey);
            dsa.update(strByte);

            output = dsa.sign();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return output;
    }

    // Verifies a String signature
    public static boolean verifySignature(PublicKey publicKey, String data, byte[] signature)
    {
        try
        {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getDifficultyString(int difficulty)
    {
        // Create a string with difficulty * "0" | eg. difficulty 5 = "00000"
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(Key key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Tacks in array of transactions and returns a merkle root.
    public static String getMerkleRoot(ArrayList<Transaction> transactions)
    {
        int count = transactions.size();

        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for (Transaction transaction : transactions)
        {
            previousTreeLayer.add(transaction.transactionId);
        }

        ArrayList<String> treeLayer = previousTreeLayer;
        while (count > 1)
        {
            treeLayer = new ArrayList<>();

            for (int i = 1; i < previousTreeLayer.size(); i++)
            {
                treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }

            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }

}
