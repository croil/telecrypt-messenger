package tele.crypt.RSA;
import java.math.BigInteger;

public class PrivateKey extends Key {
    private final BigInteger closedEXP;
    public PrivateKey(BigInteger exponent, BigInteger n) {
        super(exponent, n);
        closedEXP = exponent;
    }

    @Override
    public BigInteger getExponent() {
        return this.closedEXP;
    }
}
