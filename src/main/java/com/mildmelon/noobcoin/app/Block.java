package com.mildmelon.noobcoin.app;

import com.mildmelon.noobcoin.app.utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;


public class Block
{

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    private long timeStamp; // Number of milliseconds since 1/1/1970 (epoch time)
    private long nonce;

    public Block(String previousHash)
    {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash(); // Must be called after we set up all other variables
    }

    public String calculateHash()
    {
        return StringUtil.applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    // Increases nonce value until hash target is reached
    public void mineBlock(int difficulty)
    {
        System.out.println("\nTrying to mine new block with " + transactions.size() + " transactions");

        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);

        long time = System.currentTimeMillis();
        while (!hash.substring(0, difficulty).equals(target))
        {
            nonce++;

            if (System.currentTimeMillis() - time >= 250)
            {
                System.out.print("\rNonce: " + nonce);
                time = System.currentTimeMillis();
            }

            hash = calculateHash();
        }

        System.out.println("\rNonce: " + nonce);
        System.out.println("Block Mined: " + hash + "\n");
    }

    // Add transactions to this block
    public boolean addTransaction(Transaction transaction)
    {
        // Process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null)
        {
            return false;
        }

        if ((!previousHash.equals("0")))
        {
            if ((!transaction.processTransaction()))
            {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

}