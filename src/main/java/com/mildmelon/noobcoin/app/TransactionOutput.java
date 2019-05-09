package com.mildmelon.noobcoin.app;

import com.mildmelon.noobcoin.app.utils.StringUtil;

import java.security.PublicKey;


public class TransactionOutput
{

    public String id;
    public String parentTransactionId;      // The id of the transaction this output was created in
    public float value;                     // The amount of coins they own
    public transient PublicKey recipient;   // Also known as the new owner of these coins

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId)
    {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + value + parentTransactionId);
    }

    // Check if the coin belongs to you
    public boolean isCoinMine(PublicKey publicKey)
    {
        return (publicKey == recipient);
    }

}
