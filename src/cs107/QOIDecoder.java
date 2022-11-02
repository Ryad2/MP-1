package cs107;

import static cs107.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @apiNote Third task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder() {
    }

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     *
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header) {
        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;

        for (int i = 0; i < 4; i++) assert header[i] == QOISpecification.QOI_MAGIC[i];

        assert header[12] == QOISpecification.RGB || header[12] == QOISpecification.RGBA;
        assert header[13] == QOISpecification.ALL || header[13] == QOISpecification.sRGB;

        int[] result = new int[4];

        result[0] = ArrayUtils.toInt(ArrayUtils.extract(header, 4, 4));
        result[1] = ArrayUtils.toInt(ArrayUtils.extract(header, 8, 4));
        result[2] = header[12];
        result[3] = header[13];

        return result;
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param alpha    (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int  decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx) { // idx non comprise ???
        assert buffer != null && input != null;                                                         // also ask about return
        assert position >= 0 && position < buffer.length;
        assert idx >= 0 && idx < input.length;  // ask about this

        assert input.length - idx >= 4;

        for (int i = 0; i < 3; i++) buffer[position][i] = input[idx + i];
        buffer[position][3] = alpha;

        // why 3? should there be a calculation
        return 3;//return the number of bytes used in the input
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx) {
        assert buffer != null && input != null;                                                         // also ask about return
        assert position >= 0 && position < buffer.length;
        assert idx >= 0 && idx < input.length;  // ask about this

        assert input.length - idx >= 5;

        for (int i = 0; i < 4; i++) buffer[position][i] = input[idx + i];

        return 4;// return the number of consumed bytes
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk         (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk) {
        byte[] result = new byte[4];


        // version 2
        byte selector = 0b00_11_00_00;
        for (int i = 0; i < 3; i++) {
            int offset = 4-i*2;
            result[i] = (byte)(((selector & chunk)>>offset) + previousPixel[i] - 2);
            selector = (byte)(selector >> 2);
        }
        result[3] = previousPixel[3];


        // version 1

        /*for (int i = 0; i < 3; i++) {
            result[i] = (byte) (previousPixel[i] + (((0b00_00_00_11 << 2 * (2 - i) & chunk) >> 2 * (2 - i))) - 2);
        }
        result[3] = previousPixel[3];*/

        return result;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param data          (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data) {
        assert previousPixel != null && data != null;
        assert previousPixel.length == 4;
        assert (data[0] & QOISpecification.QOI_OP_LUMA_TAG) == QOISpecification.QOI_OP_LUMA_TAG;

        byte[] result = new byte[4];
        byte diff_green = (byte) ((data[0] & 0b00_11_11_11) - 32);
        byte diff_red = (byte) (((data[1] & 0b11_11_00_00) >> 4) + diff_green - 8);
        byte diff_bleu = (byte) ((data[1] & 0b00_00_11_11) + diff_green - 8);

        result[0] = (byte) (previousPixel[0] + diff_red);
        result[1] = (byte) (previousPixel[1] + diff_green);                   // CAN MAKE IT IN LOOP
        result[2] = (byte) (previousPixel[2] + diff_bleu);
        result[3] = previousPixel[3];


        return result;
    }

    /**
     * Store the given pixel in the buffer multiple times
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param pixel    (byte[]) - The pixel to store
     * @param chunk    (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position) {
        int result = 0b00_11_11_11 & chunk;
        assert buffer != null;
        assert position >= 0 && position < buffer.length;
        assert pixel != null && pixel.length == 4;
        assert buffer.length >= result + position;

        for (int i = position; i <= result+position; i++){
            buffer[i] = pixel;
        }

        return result; //should return the number of new pixels written in the buffer
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     *
     * @param data   (byte[]) - Data to decode
     * @param width  (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height) {
        assert data != null;
        assert width > 0 && height > 0;
        assert data.length > width * height;//NOT CORRECT

        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];
        //byte counter = 0;
        byte[][] buffer = new byte[width * height][4];
        //int[] index_tab = new int[64];

        //int position = 0;
        int position = -1;

        // ==================================================================================
        // =============================== DECODING V.2 =====================================
        // ==================================================================================

        int index = 0;
        while (index < data.length)
        {
            position++;
            byte chunk = data[index];

            // RGBA
            if (chunk == QOISpecification.QOI_OP_RGBA_TAG){
                index += decodeQoiOpRGBA(buffer, data, position, index);
            }

            // RGB
            else if (chunk == QOISpecification.QOI_OP_RGB_TAG){
                index += decodeQoiOpRGB(buffer, data, previousPixel[3], position, index);
            }

            // RUN
            else if (compareTag(chunk, QOISpecification.QOI_OP_RUN_TAG)){
                position += decodeQoiOpRun(buffer, previousPixel, chunk, position);
                index++;
            }

            // INDEX
            else if (compareTag(chunk, QOISpecification.QOI_OP_INDEX_TAG)){
                index += decodeIndex(buffer, hashTable, chunk, position);
            }

            // DIFF
            else if (compareTag(chunk, QOISpecification.QOI_OP_DIFF_TAG)){
                buffer[position] = decodeQoiOpDiff(previousPixel, chunk);
                index++;
            }

            // LUMA
            else if (compareTag(chunk, QOISpecification.QOI_OP_LUMA_TAG)){
                buffer[position] = decodeQoiOpLuma(previousPixel, ArrayUtils.concat(data[index], data[index+1]));
                index += 2;
            }


            // update the previous pixel
            previousPixel = buffer[position];

            byte hash = QOISpecification.hash(buffer[position]);
            if (!ArrayUtils.equals(hashTable[hash], buffer[position]))
            {
                hashTable[hash] = buffer[position];
            }
        }


        // ==================================================================================
        // =============================== DECODING V.1 =====================================
        // ==================================================================================

        //while (index < data.length){

        /*for (int index = 0; index < data.length; index++) {

            if (data[index] == QOISpecification.QOI_OP_RGB_TAG) {
                index += 1 + decodeQoiOpRGB(buffer, data, previousPixel[3], position, index);

                byte hash = QOISpecification.hash(buffer[position]);
                if (!ArrayUtils.equals(hashTable[hash], buffer[position])) hashTable[hash] = buffer[position];


                previousPixel = buffer[position];
                position++;
                if (index >= data.length) continue;

            }

            if (data[index] == QOISpecification.QOI_OP_RGBA_TAG) {

                index += 1 + decodeQoiOpRGBA(buffer, data, position, index);


                byte hash = QOISpecification.hash(buffer[position]);
                if (!ArrayUtils.equals(hashTable[hash], buffer[position])) hashTable[hash] = buffer[position];


                previousPixel = buffer[position];
                position++;
                if (index >= data.length) continue;
            }

            if ((byte) (data[index] & 0b11_00_00_00) == QOISpecification.QOI_OP_RUN_TAG) {
                int temp = decodeQoiOpRun(buffer, previousPixel, (byte) (data[index]), position);
                previousPixel = buffer[position];
                index++;
                position += temp+1;


                byte hash = QOISpecification.hash(buffer[position]);
                if (!ArrayUtils.equals(hashTable[hash], buffer[position])) hashTable[hash] = buffer[position];

                if (index >= data.length) continue;
            }

            if ((byte) (data[index] & 0b11_00_00_00) == QOISpecification.QOI_OP_INDEX_TAG) {
                index_tab[data[index] & 0b00_11_11_11] = position;

                byte hash = QOISpecification.hash(buffer[position]);
                if (!ArrayUtils.equals(hashTable[hash], buffer[position])) hashTable[hash] = buffer[position];

                position++;
                index++;
                if (index >= data.length) continue;
            }

            if ((byte) (data[index] & 0b11_00_00_00) == QOISpecification.QOI_OP_DIFF_TAG) {
                buffer[position] = decodeQoiOpDiff(previousPixel, (byte) (data[index] & 0b00_11_11_11));
                previousPixel = buffer[position];

                byte hash = QOISpecification.hash(buffer[position]);
                if (!ArrayUtils.equals(hashTable[hash], buffer[position])) hashTable[hash] = buffer[position];


                position++;
                index++;
                if (index >= data.length) continue;
            }

            if ((byte) (data[index] & 0b11_00_00_00) == QOISpecification.QOI_OP_LUMA_TAG) {

                //System.out.println(Integer.toBinaryString((byte)-76));

                buffer[position] = decodeQoiOpLuma(previousPixel, ArrayUtils.concat(data[index], data[index + 1]));
                previousPixel = buffer[position];

                byte hash = QOISpecification.hash(buffer[position]);
                if (!ArrayUtils.equals(hashTable[hash], buffer[position])) hashTable[hash] = buffer[position];

                position++;
                index += 2;
                if (index >= data.length) continue;
            }


        }*/
        //for(int i=0; i<index_tab.length; i++)buffer[index_tab[i]]=hashTab[i];

        return buffer;
    }

    private static boolean compareTag(byte chunk, byte tag)
    {
        byte chunkTag = (byte)(chunk & 0b11_00_00_00);      // testing variable
        return (byte)(chunk & 0b11_00_00_00) == tag;
    }

    /**
     * @return 1. The number of pixels created
     */
    private static int decodeIndex(byte[][] buffer, byte[][] hashTable, byte hash, int position)
    {
        buffer[position] = hashTable[hash];
        return 1;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     *
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content) {
        return Helper.fail("Not Implemented");
    }

}