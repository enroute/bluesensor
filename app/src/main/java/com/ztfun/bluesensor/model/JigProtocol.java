package com.ztfun.bluesensor.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ztfun.Util;

public class JigProtocol {
    private static final String TAG = JigProtocol.class.getSimpleName();

    /**
     * byte0	byte1	byte2	byte3	byte4	byte5	byte6	byte7	byte8	byte9	byte10	byte11	byte12	byte13	byte14	byte15
     * sync	time(单位ms)				voltage（单位mv）		current（单位ma）		frequency（单位hz）				temperature（单位℃）	mode	pec
     * 0x0f	bit[0-7]	bit[8-15]	bit[16-23]	bit[24-31]	bit[0-7]	bit[8-15]	bit[0-7]	bit[8-15]	bit[0-7]	bit[8-15]	bit[16-23]	bit[24-31]	bit[0-7]	bit[0-7]	bit[0-7]
     *
     * mode	0x00	0x01	0x02	0x03
     * 模式	5w	7.5w	10w	15w
     */

    /**
     * byte0	byte1	byte2	byte3
     * sync	data1	data2	pec
     * 0x0f	bit[0-7]	bit[0-7]	bit[0-7]
     *
     * data1	0x01	0x02	0x03	0x04	0x05	0x06	0x10
     * 动作	切换5w	切换7.5w	切换10w	切换15w	满电测试	FOD测试	更改设备序号为data2
     */

    public static final int MODE_5W = 0x00;
    public static final int MODE_75W = 0x01;
    public static final int MODE_10W = 0x02;
    public static final int MODE_15W = 0x03;

    public static final int CMD_SWITCH_TO_5W = 0x01;
    public static final int CMD_SWITCH_TO_75W = 0x02;
    public static final int CMD_SWITCH_TO_10W = 0x03;
    public static final int CMD_SWITCH_TO_15W = 0x04;
    public static final int CMD_FULL_POWER_TEST = 0x05;
    public static final int CMD_FOD_TEST = 0x06;
    public static final int CMD_FOD_SET_SERIAL = 0x10;

    // todo: pec verification
    public static final int[] crcTable1 = new int[]{
            0x00,0x07,0x0E,0x09, 0x1c,0x1b,0x12,0x15, 0x38,0x3F,0x36,0x31, 0x24,0x23,0x2A,0x2D
    };
    public static final int[] crcTable2 = new int[]{
            0x00,0x70,0xE0,0x90, 0xC1,0xB1,0x21,0x51, 0x83,0xF3,0x63,0x13, 0x42,0x32,0xA2,0xD2
    };

    public static byte pec(byte lastCrc, byte newByte) {
        int index;

        index = (newByte & 0xFF);
        index ^= (lastCrc & 0xFF);
        index >>= 4;
        lastCrc &= 0x0F;
        lastCrc ^= crcTable2[index];

        index = (lastCrc & 0xFF);
        index ^= (newByte & 0xFF);
        index &= 0x0F;
        lastCrc &= 0xF0;
        lastCrc ^= crcTable1[index];

        return lastCrc;
    }

    public static byte pec(byte[] data, int size) {
        byte lastCrc = 0;
        for (int i = 0; i < size; i ++) {
            lastCrc = pec(lastCrc, data[i]);
        }
        return lastCrc;
    }

    public static class JigPackage {
        public long time;
        public int volt;
        public int curr;
        public long freq;
        public int temp;
        public int mode;
        public int pec;

        @NonNull
        @Override
        public String toString() {
            return "Time:" + time + ", volt:" + volt + ", curr:" + curr +
                    ", freq:" + freq + ", temp:" + temp + ", mode:" + mode + ", pec" + pec;
        }
    }

    public static class JigCommand {
        public static final byte sync = 0x0f;
        public byte[] bytes = new byte[4];
        public JigCommand(int cmd) {
            initialize(cmd, 0);
        }
        public JigCommand(int cmd, int serial) {
            initialize(cmd, serial);
        }
        public void initialize(int cmd, int serial) {
            bytes[0] = sync;
            bytes[1] = (byte)(cmd & 0xff);
            bytes[2] = (byte)(serial & 0xff);
            bytes[3] = (byte)(pec(bytes, 3) & 0xff);
        }
    }

    private static String bytes2String(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(Util.byte2Hex(b));
            sb.append(" ");
        }
        return sb.toString();
    }

    public static JigPackage parse(byte[] data) {
        // byte 0 is sync, should be 0x0f, just ignore
        byte pecCrc = pec(data, 15);
        if (pecCrc != data[15]) {
            Log.d(TAG, "wrong pec, expecting " + pecCrc + ", while got " + data[15] + " in data: " + data.toString());
            return null;
        }

        // byte 1 - 4 is time in millisecond
//        long time = (data[1] & 0xff) << 24 + (data[2] & 0xff) << 16 + (data[3] & 0xff) << 8 + (data[4] & 0xff);
//        int volt = (data[5] & 0xff) << 8 + (data[6] & 0xff);
//        int curr = (data[7] & 0xff) << 8 + (data[8] & 0xff);
//        long freq = (data[9] & 0xff) << 24 + (data[10] & 0xff) << 16 + (data[11] & 0xff) << 8 + (data[12] & 0xff);

        int time = (data[4] & 0x00ff) << 24 | (data[3] & 0x00ff) << 16 | (data[2] & 0x00ff) << 8 | (data[1] & 0x00ff);
        int volt = (data[6] & 0x00ff) << 8 | (data[5] & 0x00ff);
        int curr = (data[8] & 0x00ff) << 8 | (data[7] & 0x00ff);
        long freq = (data[12] & 0x00ff) << 24 | (data[11] & 0x00ff) << 16 | (data[10] & 0x00ff) << 8 | (data[9] & 0x00ff);

        int temp = (data[13] & 0x00ff);
        int mode = (data[14] & 0x00ff);
        int pec = (data[15] & 0x00ff);

        JigPackage jigPackage = new JigPackage();
        jigPackage.time = time;
        jigPackage.volt = volt;
        jigPackage.curr = curr;
        jigPackage.freq = freq;
        jigPackage.temp = temp;
        jigPackage.mode = mode;
        jigPackage.pec = pec;

        Log.d(TAG, "data:" + bytes2String(data) + ", Package: " + jigPackage.toString());
        return jigPackage;
    }

    public static byte[] getJigCommandBytes(int cmd) {
       return getJigCommandBytes(cmd, 0);
    }

    public static byte[] getJigCommandBytes(int cmd, int serial) {
        JigCommand jigCommand = new JigCommand(cmd, serial);
        return jigCommand.bytes;
    }
}
