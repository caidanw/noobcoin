package com.mildmelon.noobcoin.app;

import com.google.gson.GsonBuilder;
import com.mildmelon.noobcoin.app.utils.StringUtil;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BlockChain
{

    public static final int difficulty = 5;
    public static float minimumTransaction = 0.1f;

    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>(); // List of all unspent transactions

    public static Wallet wallet1;
    public static Wallet wallet2;

    public static Transaction genesisTransaction;

    public BlockChain()
    {
        // Setup Bouncy Castle as a security provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the new wallets
        wallet1 = new Wallet();
        wallet2 = new Wallet();
        Wallet coinbase = new Wallet("Coinbase");

        // Create genesis transaction, which sends 100 NoobCoin to wallet1
        genesisTransaction = new Transaction(coinbase.publicKey, wallet1.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);  // Manually sign the genesis transaction
        genesisTransaction.transactionId = "0";                     // Manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(
                genesisTransaction.recipient,
                genesisTransaction.value,
                genesisTransaction.transactionId
        ));                                                         // Manually add the transactions output

        // Its important to store our first transaction in the UTXOs list.
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // Testing
        wallet1.printBalance();
        wallet2.printBalance();

        Block block1 = new Block(genesis.hash);
        block1.addTransaction(wallet1.sendFunds(wallet2, 40f));
        addBlock(block1);

        wallet1.printBalance();
        wallet2.printBalance();

        Block block2 = new Block(block1.hash);
        block2.addTransaction(wallet1.sendFunds(wallet2, 1000f));
        addBlock(block2);

        wallet1.printBalance();
        wallet2.printBalance();

        Block block3 = new Block(block2.hash);
        block3.addTransaction(wallet2.sendFunds(wallet1, 20));
        addBlock(block3);

        wallet1.printBalance();
        wallet2.printBalance();

        System.out.println("\nBlockchain is valid: " + isChainValid());
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("Blockchain:\n" + blockchainJson);
    }

    public static Boolean isChainValid()
    {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = StringUtil.getDifficultyString(difficulty);

        HashMap<String,TransactionOutput> tempUtxos = new HashMap<>(); // Temporary working list of unspent transactions at a given block state.
        tempUtxos.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        // Loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++)
        {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Compare registered hash and calculated hash
            if (!currentBlock.hash.equals(currentBlock.calculateHash()))
            {
                System.out.println("#Current Hashes not equal");
                return false;
            }

            // Compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash))
            {
                System.out.println("#Previous Hashes not equal");
                return false;
            }

            // Check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget))
            {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            // Loop through the blockchains transactions
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++)
            {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifySignature())
                {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }

                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue())
                {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input: currentTransaction.inputs)
                {
                    tempOutput = tempUtxos.get(input.transactionOutputId);

                    if (tempOutput == null)
                    {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.utxo.value != tempOutput.value)
                    {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUtxos.remove(input.transactionOutputId);
                }

                for (TransactionOutput output: currentTransaction.outputs)
                {
                    tempUtxos.put(output.id, output);
                }

                if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient)
                {
                    System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
                    return false;
                }

                if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender)
                {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }

        return true;
    }

    public static void addBlock(Block newBlock)
    {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public static void main(String[] args)
    {
        SpringApplication.run(BlockChain.class, args);
    }

}
