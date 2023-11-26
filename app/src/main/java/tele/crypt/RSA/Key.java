package tele.crypt.RSA;
import java.math.BigInteger;

public abstract class Key {
    private final BigInteger n;
    public Key(BigInteger exponent, BigInteger n) {
        this.n = n;
    }
    public abstract BigInteger getExponent();

    public BigInteger getN() {
        return this.n;
    }
}
