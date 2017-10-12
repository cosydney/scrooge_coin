import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        UTXOPool uniqueUtxos = new UTXOPool();
        double prevTxOutSum = 0;
        double curTxOutSum = 0;
        for (int i = 0; i < tx.numInputs() ; i++ ) {
          Transaction.Input in = tx.getInput(i);
          UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
          Transaction.Ouput output = utxoPool.getTxOutput(utxo);
          // * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
          if (!utxoPool.contains(utxo)) return false;
          //  * (2) the signatures on each input of {@code tx} are valid,
          if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), in.signature))
            return false;
          //  * (3) no UTXO is claimed multiple times by {@code tx},
          if (uniqueUtxos.contains(utxo)) return false;
          uniqueUtxos.addUTXO(utxo, output);
          prevTxOutSum += output.value;
        }

        for (Transaction.Ouput out : tx.getOutputs()) {
          // * (4) all of {@code tx}s output values are non-negative, and
          if (out.value < 0) return false;
          curTxOutSum += out.value;
        }
        // * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        return prevTxOutSum >= curTxOutSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Set<Transaction> correctTxs = new HashSet<>();

        for (Transaction tx : possibleTxs) {
          if (isValidTx(tx))
          correctTxs.add(tx);
          for (Transaction.Input in : tx.getInputs()) {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            utxoPool.removeUTXO(utxo);
          }
          for (int i = 0; i < tx.numOutputs() ; i++ ) {
            Transaction.Output out = tx.getOutput(i);
            UTXO utxo = new UTXO(tx.getHash(), i);
            utxoPool.addUTXO(utxo, out);
          }

        }
    }

    Transaction[] validTxArray = new Transaction[correctTxs.size()];
    return correctTxs.toArray(validTxArray);

}
