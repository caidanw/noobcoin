package com.mildmelon.noobcoin.app;

import com.mildmelon.noobcoin.app.utils.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;


public class Transaction
{

    public String transactionId; // This is also the hash of the transaction
    public PublicKey sender;     // The senders address/public key
    public PublicKey recipient;  // The recipients address/public key
    public float value;
    public byte[] signature;     // THis is to prevent anybody else from spending funds in our wallet

    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // Rough count of total generated transactions

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs)
    {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    private String calculateHash()
    {
        sequence++; // Increase the sequence to avoid duplicate hashes from identical transactions

        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender)
                        + StringUtil.getStringFromKey(recipient)
                        + value
                        + sequence
        );
    }

    private String getData()
    {
        return StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
    }

    public void generateSignature(PrivateKey privateKey)
    {
        String data = getData();
        signature = StringUtil.applySignature(privateKey, data);
    }

    public boolean verifySignature()
    {
        String data = getData();
        return StringUtil.verifySignature(sender, data, signature);
    }

    // Returns true if new transaction could be created
    public boolean processTransaction()
    {
        if (!verifySignature())
        {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // Gather transaction inputs (Make sure they are unspent)
        for (TransactionInput i : inputs)
        {
            i.utxo = BlockChain.UTXOs.get(i.transactionOutputId);
        }

        // Check if transaction is valid
        if (getInputsValue() < BlockChain.minimumTransaction)
        {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // Generate transaction outputs
        float leftOver = getInputsValue() - value; // Get value of inputs then the left over change
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value,transactionId)); // Send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver,transactionId)); // Send the left over 'change' back to sender

        // Add outputs to Unspent list
        for (TransactionOutput o : outputs)
        {
            BlockChain.UTXOs.put(o.id, o);
        }

        // Remove transaction inputs from UTXO lists as spent
        for (TransactionInput i : inputs)
        {
            if (i.utxo == null)
            {
                continue; // If Transaction can't be found skip it
            }
            BlockChain.UTXOs.remove(i.utxo.id);
        }

        return true;
    }

    // Returns sum of inputs(UTXOs) values
    public float getInputsValue()
    {
        float total = 0;
        for (TransactionInput i : inputs)
        {
            if (i.utxo == null)
            {
                continue; // If Transaction can't be found skip it
            }
            total += i.utxo.value;
        }
        return total;
    }

    // Returns sum of outputs
    public float getOutputsValue()
    {
        float total = 0;
        for (TransactionOutput o : outputs)
        {
            total += o.value;
        }
        return total;
    }

}
