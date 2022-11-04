package cs107;

/**
 * Utility class to manipulate arrays.
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @apiNote First Task of the 2022 Mini Project
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils() {
    }

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2) {//ask assistant if not the same size

        assert a1 != null ^ a2 == null : "one of the arrays is null";
        if (a1.length != a2.length) return false ;


        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) return false;
        }

        return true;

    }

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2) {
        assert  a1==null ^ a2==null : "one of the parameters is null" ;
        if (a1.length != a2.length) return false;


        for (int i = 0; i < a1.length; i++) {

            if (!equals(a1[i], a2[i])) return false;
        }

        return true;

    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     *
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value) {

        return new byte[]{value};

    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes) {
        assert bytes != null && bytes.length==4 : "the input is null or the input's length is different from 4 ";

        int x = 0;
        for (byte aByte : bytes) {
            x = (x << 8) + aByte;
        }
        return x;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value) {



        byte[] result = new byte[4];

        for (int i = 3; i >= 0; i--) {
            result[i] = (byte) (value & 0b11111111);
            value = value >> 8;
        }
        return result;

    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     *
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte... bytes) {
        assert bytes!= null;

        byte[] result = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[i];
        }
        return result;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     *
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[]... tabs) {

        assert tabs != null;

        for (byte[] tb : tabs) {
            assert tb != null;
        }


        int l = 0;
        for (byte[] tb : tabs) l += tb.length;
        byte[] result = new byte[l];

        int x = 0;
        for (byte[] tab : tabs) {
            for (int j = 0; j < tab.length; j++) {
                result[x + j] = tab[j];
            }
            x += tab.length;
        }
        return result;

    }
    //ESSAYER D UTILISER CONCACT

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     *
     * @param input  (byte[]) - Array to extract from
     * @param start  (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     *                        start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length) {

        assert input.length != 0;
        assert (start + length) <= input.length;
        assert start >= 0 && length >= 0 && start < input.length;

        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = input[i + start];
        }
        return result;

    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     *
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     *                        or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int... sizes) {

        int sum = 0;
        for (int sz : sizes) sum += sz;
        assert input.length != 0 && sizes.length != 0 && input.length == sum;


        int x = 0;
        byte[][] result = new byte[sizes.length][];

        for (int i = 0; i < sizes.length; i++) {
            result[i] = new byte[sizes[i]];

            for (int j = 0; j < sizes[i]; j++) {
                result[i][j] = input[x + j];
            }
            x += sizes[i];
        }

        return result;

    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     *
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input) {
        assert input != null;
        int inputLength = input[0].length;
        for (int[] array : input) {
            assert array != null;
            assert array.length == inputLength;
        }


        byte[][] result = new byte[input.length * input[0].length][];
        int index = 0;
        for (int[] width : input) {
            for (int height :width) {
                result[index] = CustomHelpers.ARBGtoRGBA(fromInt(height));
                index++;
            }
        }
        return result;
    }

    private static byte[] ARBGtoRGBA(byte[] array) {
        byte[] result = new byte[array.length];

        for (int i = 0; i < 3; i++){
            result[i] = array[i+1];
        }
        result[3] = array[0];

        return result;
    }   // try to remove later, also combine.
    private static byte[] RGBAtoARGB(byte[] array){
        byte[] result = new byte[array.length];

        for (int i = 1; i < 4; i++){
            result[i] = array[i-1];
        }
        result[0] = array[3];

        return result;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     *
     * @param input  (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width  (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     *                        or input's length differs from width * height
     *                        or height is invalid
     *                        or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width) {
        assert input!=null;
        assert input.length==height*width;
        for (byte[]in:input) assert in!=null && in.length==4;


        int[][] result = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++){
                result[i][j] = toInt(CustomHelpers.RGBAtoARGB(input[i * width+j]));
            }
        }
        return result;
    }

}