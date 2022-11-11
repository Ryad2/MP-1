package cs107;

public class CustomHelpers {

    // ==================================================================================
    // ============================== ArrayUtils helpers ================================
    // ==================================================================================

    public static byte[] ARBGtoRGBA(byte[] array) {
    byte[] result = new byte[array.length];

    for (int i = 0; i < 3; i++){
        result[i] = array[i+1];
    }
    result[3] = array[0];

    return result;
    }

    public static byte[] RGBAtoARGB(byte[] array){
        byte[] result = new byte[array.length];

        for (int i = 1; i < 4; i++){
            result[i] = array[i-1];
        }
        result[0] = array[3];

        return result;
    }

    public static byte[] moveColorChannels(byte[] array, boolean moveToRGBA){
        int index;

        if (moveToRGBA) index = 0;
        else index = 2;

        byte[] result = new byte[4];

        for (int i = 0; i < 4; i++){
            index++;
            if (index == 4) index = 0;

            result[i] = array[index];
        }

        return result;
    }


    // ==================================================================================
    // ============================== Encoder helpers ===================================
    // ==================================================================================


    public static boolean diff (byte[] current, byte[] previous, byte[] difference){
        getDiff(current, previous, difference);
        for (byte valueDifference : difference) {
            if (valueDifference > 1 || valueDifference < -2) return false;
        }
        return true;
    }
    public static void getDiff(byte[] current, byte[] previous, byte[] difference) {
        for (int i = 0; i < 3; i++){
            difference[i] = (byte)(current[i] - previous[i]);
        }
    }

    public static boolean luma(byte[] current, byte[] previous, byte[] difference){
        getDiff(current, previous, difference);

        if (difference[1] >= 32 || difference[1] <= -33) return false;
        for (int i = 0; i < 3; i += 2){
            if (difference[i] - difference[1] >= 8 || difference[i] - difference[1] <= -9) return false;
        }
        return true;
    }


    // test functions
    public static void testChannelOffset() {

        byte[] test1 = new byte[] {0, 1, 2, 3};     // ARGB
        byte[] test2 = new byte[] {1, 2, 3, 0};     // RGBA

        byte[] result1 = moveColorChannels(test1, true);
        byte[] result2 = moveColorChannels(test2, false);

        return;
    }
}
