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


    // ==================================================================================
    // ============================== Encoder helpers ===================================
    // ==================================================================================


    public static boolean compareTag(byte chunk, byte tag) {
        return (byte)(chunk & 0b11_00_00_00) == tag;
    }
}
