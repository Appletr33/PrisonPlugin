package org.teameugene.prison.Util;

public class BytePacking {
    public static long packBytes(byte[] bytes) {
        if (bytes.length > 8) {
            throw new IllegalArgumentException("Bytes array cannot contain more than 8 bytes.");
        }

        byte[] paddedBytes = new byte[8];
        System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length); // Copy the original bytes

        long packedValue = 0;
        for (int i = 0; i < 8; i++) {
            packedValue |= ((long) (paddedBytes[i] & 0xFF)) << (i * 8);
        }
        return packedValue;
    }

    public static byte[] unpackBytes(long packedValue) {
        byte[] unpackedBytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            unpackedBytes[i] = (byte) ((packedValue >> (i * 8)) & 0xFF);
        }
        return unpackedBytes;
    }
}
