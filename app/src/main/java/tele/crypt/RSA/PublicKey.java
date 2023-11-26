package tele.crypt.RSA;
import java.math.BigInteger;

public class PublicKey extends Key {
    private final BigInteger openedEXP;
    public PublicKey(BigInteger exponent, BigInteger n) {
        super(exponent, n);
        openedEXP = exponent;
    }

    @Override
    public BigInteger getExponent() {
        return this.openedEXP;
    }
}
