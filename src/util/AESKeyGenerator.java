package util;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * 根据输入的字符串生成一个 AES key 对象。
 *
 * @author soulike
 */
public class AESKeyGenerator
{
    /**
     * 根据输入的字符串生成一个 AES key 对象。
     *
     * @param password 要作为密码的字符串。
     */
    public static Key getAESKey(String password)
    {
        final byte[] keyData = get16BytesKeyData(password);
        return new SecretKeySpec(keyData, "AES");
    }

    /**
     * 把字符串变为 16 bit 的 byte 数组。
     *
     * @param password 要作为密码的字符串。
     */
    private static byte[] get16BytesKeyData(String password)
    {
        final byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        final byte[] BytesKeyData = new byte[16];
        int copyLength = passwordBytes.length >= 16 ? 16 : passwordBytes.length;
        for (int i = 0; i < copyLength; i++)
        {
            BytesKeyData[i] = passwordBytes[i];
        }

        // 不够 16 个就往后补 0
        for (int i = copyLength; i < 16; i++)
        {
            BytesKeyData[i] = 0;
        }

        return BytesKeyData;
    }
}
