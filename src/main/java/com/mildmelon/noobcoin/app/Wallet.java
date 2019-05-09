package com.mildmelon.noobcoin.app;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet
{
    
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> utxos = new HashMap<>(); // Only UTXOs owned by this wallet
    
    public Wallet()
    {
        generateKeyPair();
    }

    public void generateKeyPair()
    {
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // Initialize the key generator and generate a new key pair
            keyGen.initialize(ecSpec, random); // 256 bytes provides an acceptable level of security
            KeyPair keyPair = keyGen.generateKeyPair();

            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // Returns balance and stores the UTXO's owned by this wallet in this.UTXOs
    public float getBalance()
    {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: BlockChain.UTXOs.entrySet())
        {
            TransactionOutput utxo = item.getValue();
            if (utxo.isCoinMine(publicKey)) // If output belongs to me (if coins belong to me)
            {
                utxos.put(utxo.id,utxo);    // Add it to our list of unspent transactions.
                total += utxo.value;
            }
        }
        return total;
    }

    // Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey recipient, float value)
    {
        if (getBalance() < value) // Gather balance and check funds.
        {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        // Create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: utxos.entrySet())
        {
            TransactionOutput utxo = item.getValue();

            total += utxo.value;
            inputs.add(new TransactionInput(utxo.id));

            if (total > value)
            {
                break;
            }
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input: inputs)
        {
            utxos.remove(input.transactionOutputId);
        }

        return newTransaction;
    }

}