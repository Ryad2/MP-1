package cs107;

import java.util.ArrayList;

/**
 * "Quite Ok Image" Encoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @apiNote Second task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder() {
    }

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     *
     * @param image (Helper.Image) - Image to use
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *                        (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     */
    public static byte[] qoiHeader(Helper.Image image) {

        assert image != null;
        assert image.channels() == QOISpecification.RGB || image.channels() == QOISpecification.RGBA;
        assert image.color_space() == QOISpecification.sRGB || image.color_space() == QOISpecification.ALL;


        byte[] width = ArrayUtils.fromInt(image.data()[0].length);
        byte[] height = ArrayUtils.fromInt(image.data().length);

        return ArrayUtils.concat(QOISpecification.QOI_MAGIC, width, height,
                ArrayUtils.wrap(image.channels()), ArrayUtils.wrap(image.color_space()));
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     *
     * @param pixel (byte[]) - The Pixel to encode
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     * @throws AssertionError if the pixel's length is not 4
     */
    public static byte[] qoiOpRGB(byte[] pixel) {

        assert pixel.length == 4;

        byte[] result = new byte[4];

        result[0] = QOISpecification.QOI_OP_RGB_TAG;
        for (int i = 0; i < 3; i++) result[i + 1] = pixel[i];

        return result;
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     *
     * @param pixel (byte[]) - The pixel to encode
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     * @throws AssertionError if the pixel's length is not 4
     */
    public static byte[] qoiOpRGBA(byte[] pixel) {

        assert pixel.length == 4;

        byte[] result = new byte[5];

        result[0] = QOISpecification.QOI_OP_RGBA_TAG;
        for (int i = 0; i < 4; i++) result[i + 1] = pixel[i];

        return result;
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     *
     * @param index (byte) - Index of the pixel
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     * @throws AssertionError if the index is outside the range of all possible indices
     */
    public static byte[] qoiOpIndex(byte index) {
        assert index >= 0 && index <= 63;
        return ArrayUtils.wrap(index);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     *
     * @param diff (byte[]) - The difference between 2 pixels
     * @return (byte[]) - Encoding of the given difference
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     *                        (See the handout for the constraints)
     */
    public static byte[] qoiOpDiff(byte[] diff) {        // try to make it prettier
        assert diff != null;
        assert diff.length == 3;
        for (byte di : diff) assert di <= 1 && di >= -2;


        byte result = QOISpecification.QOI_OP_DIFF_TAG;
        for (int i = diff.length - 1; i >= 0; i--) {
            byte x = (byte) ((diff[2 - i] + 2) << 2 * i);
            result = (byte) (result | x);
        }


        return ArrayUtils.wrap(result);
    }//MAKE IT BETTER

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     *
     * @param diff (byte[]) - The difference between 2 pixels
     * @return (byte[]) - Encoding of the given difference
     * @throws AssertionError if diff doesn't respect the constraints
     *                        or diff's length is not 3
     *                        (See the handout for the constraints)
     */
    public static byte[] qoiOpLuma(byte[] diff) {
        assert diff != null;
        assert diff.length == 3;
        assert diff[1] > -33 && diff[1] < 32;
        for (int i = 0; i < diff.length; i += 2) assert diff[i] - diff[1] > -9 && diff[i] - diff[1] < 8;

        byte[] result = new byte[2];
        result[0] = (byte) (QOISpecification.QOI_OP_LUMA_TAG + (32 + diff[1]));
        result[1] = (byte) (((diff[0] - diff[1] + 8) << 4) + diff[2] - diff[1] + 8);

        return result;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     *
     * @param count (byte) - Number of similar pixels
     * @return (byte[]) - Encoding of count
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     */
    public static byte[] qoiOpRun(byte count) {
        assert count >= 1 && count <= 62;
        byte result = (byte) (QOISpecification.QOI_OP_RUN_TAG + (count - 1));

        return ArrayUtils.wrap(result);
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     *
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {
        assert image != null;
        for (byte[] im : image) assert im != null && im.length == 4;

        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];
        byte counter = 0;
        ArrayList<byte[]> encodedData = new ArrayList<>();

        // ==================================================================================
        // =============================== ENCODING V.2 =====================================
        // ==================================================================================

        byte[] difference = new byte[3];

        for (int index = 0; index < image.length; index++) {
            byte[] pixel = image[index];
            byte hash = QOISpecification.hash(pixel);

            // RUN
            if (ArrayUtils.equals(pixel, previousPixel)) {
                counter++;

                if (counter == 62 || index == image.length - 1) {
                    encodedData.add(qoiOpRun(counter));
                    counter = 0;
                }

                previousPixel = pixel;
                continue;
            } else if (counter != 0) {
                encodedData.add(qoiOpRun(counter));
                counter = 0;
            }

            // INDEX
            if (ArrayUtils.equals(hashTable[hash], pixel)) {
                encodedData.add(qoiOpIndex(hash));

                previousPixel = pixel;
                continue;
            } else {
                hashTable[hash] = pixel;
            }

            // DIFF
            if (CustomHelpers.diff(pixel, previousPixel, difference) && pixel[3] == previousPixel[3]) {
                encodedData.add(qoiOpDiff(difference));
            }

            // LUMA
            else if (CustomHelpers.luma(pixel, previousPixel, difference) && pixel[3] == previousPixel[3]) {
                encodedData.add(qoiOpLuma(difference));
            }

            // RGB
            else if (pixel[3] == previousPixel[3]) {
                encodedData.add(qoiOpRGB(pixel));
            }

            // RGBA
            else {
                encodedData.add(qoiOpRGBA(pixel));
            }

            previousPixel = pixel;
        }

        // turn encoded data into byte[]
        byte[][] result = new byte[encodedData.size()][];
        int i = 0;
        for (byte[] data : encodedData) {
            result[i] = data;
            i++;
        }

        return ArrayUtils.concat(result);
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     *
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     */
    public static byte[] qoiFile(Helper.Image image) {
        assert image != null;

        int[][] dalas = image.data();
        byte[] encodedData = encodeData(ArrayUtils.imageToChannels(dalas));


        return ArrayUtils.concat(qoiHeader(image), encodedData, QOISpecification.QOI_EOF);
    }
}