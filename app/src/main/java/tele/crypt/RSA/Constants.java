package tele.crypt.RSA;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public final static int SIZE = 256;
    public final static int logSIZE = 200;
    public final static BigInteger ZERO = new BigInteger("0");
    public final static BigInteger ONE = new BigInteger("1");
    public final static BigInteger TWO = new BigInteger("2");
    public final static BigInteger[] PRIMES = {
            new BigInteger("17"),
            new BigInteger("257"),
            new BigInteger("65537")
    };

}
