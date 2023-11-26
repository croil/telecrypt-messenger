package tele.crypt.RSA;

import java.math.BigInteger;

public class RSA {
    private static RSA instance;
    private final NumberGenerator generator = new NumberGenerator();
    private final Alphabet abc = new Alphabet();
    private BigInteger n;
    private BigInteger euler;
//    private PublicKey publicKey = createPublicKey();
//    private PrivateKey privateKey = createPrivateKey(publicKey.getExponent());
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public static RSA getRSA() {
        if (instance == null) {
            instance = new RSA();
        }
        return instance;
    }


    public void generate() {
        BigInteger p = generator.numberGenerator();
        BigInteger q = generator.numberGenerator();
        this.n = p.multiply(q);
        this.euler = eulerFunction(p, q);
        this.publicKey = createPublicKey();
        this.privateKey = createPrivateKey(publicKey.getExponent());
    }

    public PrivateKey createPrivateKey(BigInteger openEXP) {
        BigInteger d = Constants.ZERO;
        BigInteger r = euler;
        BigInteger newD = new BigInteger("1");
        BigInteger newR = openEXP;
        while (!newR.equals(Constants.ZERO) ) {
            BigInteger div = r.divide(newR);
            BigInteger tempNewD = newD;
            newD = d.subtract(div.multiply(newD));
            d = tempNewD;
            BigInteger tempNewR = newR;
            newR = r.subtract(div.multiply(newR));
            r = tempNewR;
        }

        if (d.compareTo(Constants.ZERO) < 0) {
            d = d.add(euler);
        }
        return new PrivateKey(d, n);
    }

    private PublicKey createPublicKey() {
        BigInteger e = new BigInteger("3");
        for (BigInteger elem : Constants.PRIMES) {
            assert euler != null;
            if (!euler.mod(elem).equals(Constants.ZERO)) {
                e = elem;
                break;
            }
        }
        return new PublicKey(e, n);
    }

    public String getPublicKey() {
        return this.publicKey.getExponent() + " " + this.publicKey.getN();
    }

    public String getPrivateKey() {
        return this.privateKey.getExponent() + " " + this.privateKey.getN();
    }

    public void setPrivateKey(PrivateKey prKey) {
        this.privateKey = prKey;
    }

    public void setPublicKey(PublicKey pbKey) {
        this.publicKey = pbKey;
    }

    private static BigInteger eulerFunction(BigInteger p, BigInteger q) {
        BigInteger incP = p.subtract(Constants.ONE);
        return incP.multiply(q.subtract(Constants.ONE));
    }

    public BigInteger[] encode(String input) {
        int chunkLength = 100;
        int length = input.length();
        int j = 0;
        if (length <= chunkLength) {
            return new BigInteger[]{
                    generator.pow(bigIntMessage(input), publicKey.getExponent(), publicKey.getN())
            };
        }
        BigInteger[] message = new BigInteger[input.length() / chunkLength + 1];
        for (int i = 0; i < input.length(); i += chunkLength) {
            message[j++] = generator.pow(
                    bigIntMessage(input.substring(i, Math.min(i + chunkLength, length))),
                    publicKey.getExponent(),
                    publicKey.getN());
        }
        return message;
    }

    public String decode(BigInteger[] message) throws StringIndexOutOfBoundsException {
        String receive;
        StringBuilder ready = new StringBuilder();
        for (BigInteger chunk : message) {
            if (chunk.equals(BigInteger.ZERO)) continue;
            receive = generator.pow(chunk, privateKey.getExponent(), privateKey.getN()).toString();
            for (int i = 0; i < receive.length(); i += 3) {
                ready.append(abc.getREVERSE_DICT().get(Integer.parseInt(receive.substring(i, i + 3))));
            }
        }
        return ready.toString();
    }

    private BigInteger bigIntMessage(String str) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            message.append(
                    abc.getDICT().containsKey(str.charAt(i))
                            ? abc.getDICT().get(str.charAt(i))
                            : "");
        }
        return new BigInteger(message.toString());
    }
}
