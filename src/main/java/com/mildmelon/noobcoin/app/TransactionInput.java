package com.mildmelon.noobcoin.app;

public class TransactionInput
{

    public String transactionOutputId;  // Reference to TransactionOutputs -> transactionId
    public TransactionOutput utxo;      // Contains the unspent transaction output

    public TransactionInput(String transactionOutputId)
    {
        this.transactionOutputId = transactionOutputId;
    }

}
