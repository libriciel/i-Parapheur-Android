package org.adullact.iparapheur.tab.util;

/**
 * Created with IntelliJ IDEA.
 * User: jmaire
 * Date: 02/05/13
 * Time: 10:19
 */
public class TransformUtils {

    private static final byte[] encodingTable = {(byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
            (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
            (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q',
            (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V',
            (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a',
            (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
            (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k',
            (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p',
            (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
            (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
            (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9',
            (byte) '+', (byte) '/'
    };

    // Mapping table from 6-bit nibbles to Base64 characters.
    private static char[] map1 = new char[64];
    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            map1[i++] = c;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            map1[i++] = c;
        }
        for (char c = '0'; c <= '9'; c++) {
            map1[i++] = c;
        }
        map1[i++] = '+';
        map1[i++] = '/';
    }



    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if ((len % 2) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }
        // 2 caractères hexadécimaux sont utilisés pour représenter un octet
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] der2pem(byte[] data) {
        byte[] bytes;
        String pemType = "PKCS7";
        int modulus = data.length % 3;
        int add = 0;
        if (pemType != null) {
            if (pemType.length() > 0) {
                add = data.length / 45;
                add += 17; /* ----BEGIN ...-----\n */
                add += pemType.length();
                add += 16; /* \n----END ...-----\n */
                add += pemType.length();
            }
        }
        if (modulus == 0) {
            bytes = new byte[(4 * data.length / 3) + add];
        } else {
            bytes = new byte[(4 * ((data.length / 3) + 1)) + add];
        }

        int dataLength = (data.length - modulus);
        int j, a1, a2, a3;
        j = 0;
        if (add != 0) {
            byte[] btmp;
            String stmp;
            stmp = "-----BEGIN " + pemType + "-----";
            btmp = stmp.getBytes();
            for (j = 0; j < btmp.length; j++) {
                bytes[j] = btmp[j];
            }
        }
        for (int i = 0; i < dataLength; i += 3, j += 4) {
            a1 = data[i] & 0xff;
            a2 = data[i + 1] & 0xff;
            a3 = data[i + 2] & 0xff;

            if (add != 0 && (i % 45) == 0) {
                bytes[j++] = 0x0a;
            }
            bytes[j] = encodingTable[(a1 >>> 2) & 0x3f];
            bytes[j + 1] = encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f];
            bytes[j + 2] = encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f];
            bytes[j + 3] = encodingTable[a3 & 0x3f];
        }
        /*
         * process the tail end.
         */
        int b1, b2, b3;
        int d1, d2;

        switch (modulus) {
            case 0: /* nothing left to do */
                break;
            case 1:
                d1 = data[data.length - 1] & 0xff;
                b1 = (d1 >>> 2) & 0x3f;
                b2 = (d1 << 4) & 0x3f;

                bytes[j++] = encodingTable[b1];
                bytes[j++] = encodingTable[b2];
                bytes[j++] = (byte) '=';
                bytes[j++] = (byte) '=';
                break;
            case 2:
                d1 = data[data.length - 2] & 0xff;
                d2 = data[data.length - 1] & 0xff;

                b1 = (d1 >>> 2) & 0x3f;
                b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
                b3 = (d2 << 2) & 0x3f;

                bytes[j++] = encodingTable[b1];
                bytes[j++] = encodingTable[b2];
                bytes[j++] = encodingTable[b3];
                bytes[j++] = (byte) '=';
                break;
        }
        if (add != 0) {
            byte[] btmp;
            String stmp;
            stmp = "\n-----END " + pemType + "-----\n";
            btmp = stmp.getBytes();
            for (a1 = 0; a1 < btmp.length; a1++, j++) {
                bytes[j] = btmp[a1];
            }
        }
        return bytes;
    }

    public static String encode(byte[] in) {
        int iLen = in.length;
        int oDataLen = (iLen * 4 + 2) / 3;       // output length without padding
        int oLen = ((iLen + 2) / 3) * 4;         // output length including padding
        char[] out = new char[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iLen ? in[ip++] & 0xff : 0;
            int i2 = ip < iLen ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return new String(out);
    }
}
