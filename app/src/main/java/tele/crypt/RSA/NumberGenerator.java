package tele.crypt.RSA;
import java.math.BigInteger;


public class NumberGenerator {
    private BigInteger getRandomNumber(int size) {
        StringBuilder str = new StringBuilder();
        str.append(1 + (int)(Math.random()*9));
        for (int i = 1; i < size; i++) {
            str.append((int)(Math.random()*10));
        }
        return new BigInteger(str.toString());
    }

    public BigInteger numberGenerator() {
        while (true) {
            BigInteger a, ost;
            do {
                a = getRandomNumber(Constants.SIZE);
                ost = a.mod(Constants.TWO);
            } while (ost.equals(Constants.ZERO));
            if (millerRubinTest(a))
                return a;
        }
    }

    private boolean millerRubinTest(BigInteger test) {
        BigInteger inc = test.subtract(Constants.ONE);
        BigInteger main = inc;
        int s = 0;
        while (inc.mod(Constants.TWO).equals(Constants.ZERO)) {
            s++;
            inc = inc.divide(Constants.TWO);
        }
        for (int i = 0; i < Constants.logSIZE; i++) {

            BigInteger current = getRandomNumber(2 + (int) (Math.random() * Constants.SIZE));
            BigInteger x = pow(current, inc, test);
            BigInteger mod = x.mod(test);
            if (mod.equals(Constants.ONE) || mod.equals(main)) {
                continue;
            }
            boolean next = false;
            for (int j = 1; j < s; j++) {
                x = x.multiply(x);
                mod = x.mod(test);
                if (mod.equals(Constants.ONE)) return false;
                if (mod.equals(main)) {
                    next = true;
                    break;
                }
            }
            if (!next) return false;
        }
        return true;
    }

    public BigInteger pow(BigInteger a, BigInteger b, BigInteger c) {
        if (b.equals(Constants.ONE)) return a.mod(c);
        BigInteger div = b.divide(Constants.TWO);
        BigInteger result = pow(a, div, c);
        BigInteger res2 = result.multiply(result);
        if (!b.mod(Constants.TWO).equals(Constants.ZERO)) {
            res2 = a.multiply(res2);
        }
        return res2.mod(c);
    }
}
