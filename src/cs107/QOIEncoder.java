package cs107;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.2
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image){

        assert image!=null;
        assert image.channels()==QOISpecification.RGB || image.channels()==QOISpecification.RGBA;
        assert image.color_space()==QOISpecification.sRGB || image.color_space()==QOISpecification.ALL;


        byte[] width = ArrayUtils.fromInt(image.data()[0].length);
        byte[] height = ArrayUtils.fromInt(image.data().length);

        byte[] result = new byte[QOISpecification.HEADER_SIZE];

        result = ArrayUtils.concat(QOISpecification.QOI_MAGIC, width, height,
                ArrayUtils.wrap(image.channels()), ArrayUtils.wrap(image.color_space()));

        /*for (int i = 0; i < 4; i++)  result[i] = QOISpecification.QOI_MAGIC[i];
        for (int i = 4; i < 8; i++)  result[i] = width[i-4];
        for (int i = 8; i < 12; i++)  result[i] = height[i-8];
        result[12] = image.channels();
        result[13] = image.color_space();*/

        return result;
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel){

        assert pixel.length==4;

        byte[] result = new byte[4];

        result[0] = QOISpecification.QOI_OP_RGB_TAG;
        for (int i = 0; i < 3; i++)  result[i+1] = pixel[i];

        return result;
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel){

        assert pixel.length==4;

        byte[] result = new byte[5];

        result[0] = QOISpecification.QOI_OP_RGBA_TAG;
        for (int i = 0; i < 4; i++) result[i+1] = pixel[i];

        return result;
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){
        assert index >= 0 && index <= 63;
        return ArrayUtils.wrap(index);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){        // try to make it prettier
        assert diff!=null;
        assert diff.length==3;
        for ( byte di:diff) assert di<=1 && di>=-2;


        byte result=QOISpecification.QOI_OP_DIFF_TAG;
        for(int i=diff.length-1; i>=0; i--){
            byte x= (byte)((diff[2-i]+2)<<2*i);
            result= (byte) (result | x) ;
        }


        return ArrayUtils.wrap(result);
    }//MAKE IT BETTER

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){
        assert diff!=null;
        assert diff.length==3;
        assert diff[1]>-33 && diff[1]<32 ;
        for(int i=0;i<diff.length;i+=2)assert diff[i]-diff[1]>-9 && diff[i]-diff[1]<8 ;

        byte[] result=new byte[2];
        result[0]=(byte)(QOISpecification.QOI_OP_LUMA_TAG + (32+diff[1]));
        result[1]=(byte)(((diff[0]-diff[1]+8)<<4) + diff[2]-diff[1]+8);

        return result;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){
        assert count>=1 && count<=62;
        byte result =(byte)(QOISpecification.QOI_OP_RUN_TAG+(count-1));

        return ArrayUtils.wrap(result);
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
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

        for (int index = 0; index < image.length; index++){
            byte[] pixel = image[index];
            byte hash = QOISpecification.hash(pixel);

            // RUN
            if (ArrayUtils.equals(pixel, previousPixel)) {
                counter++;

                if (counter == 62 || index == image.length - 1) {
                    encodedData.add(qoiOpRun(counter));
                    counter = 0;
                    System.out.println("run");
                }

                previousPixel = pixel;
                continue;
            }
            else if (counter != 0) {
                encodedData.add(qoiOpRun(counter));
                counter = 0;
            }

            // INDEX
            if (ArrayUtils.equals(hashTable[hash], pixel)){
                encodedData.add(qoiOpIndex(hash));

                System.out.println("index");
                previousPixel = pixel;
                continue;
            }
            else{
                hashTable[hash] = pixel;
            }

            // RGBA
            if (pixel[3] != previousPixel[3]){
                encodedData.add(qoiOpRGBA(pixel));
                System.out.println("rbga");
            }

            // DIFF
            else if (CustomHelpers.diff(pixel, previousPixel, difference)){
                encodedData.add(qoiOpDiff(difference));
                System.out.println("diff");
            }

            // LUMA
            else if (CustomHelpers.luma(pixel, previousPixel, difference)){
                encodedData.add(qoiOpLuma(difference));
                System.out.println("luma");
            }

            // RGB
            else{
                encodedData.add(qoiOpRGB(pixel));
                System.out.println("rgb");
            }

            previousPixel = pixel;
        }

        // ==================================================================================
        // =============================== ENCODING V.1 =====================================
        // ==================================================================================

        /*
        for (int i = 0; i < image.length; i++) {

            // QOI_OP_RUN Block step1

            if (ArrayUtils.equals(image[i], previousPixel)) {
                counter++;
                if (counter == 62 || i == image.length - 1) {
                    encodedData.add(qoiOpRun(counter));
                    counter = 0;
                }
                previousPixel = image[i];
                continue;
            }
            if (counter != 0) {
                encodedData.add(qoiOpRun(counter));
                counter = 0;
            }


            // QOI_OP_INDEX Block step2
            byte hash = QOISpecification.hash(image[i]);
            if (!ArrayUtils.equals(hashTable[hash], image[i])) {              // do we need the ArrayUtils.equals here?
                hashTable[hash] = image[i];
            } else {
                encodedData.add(qoiOpIndex(hash));
                previousPixel = image[i];
                continue;
            }


            //QOI_OP_DIFF Block step3
            if (image[i][3] == previousPixel[3] && diff(image[i], previousPixel)) {//LOOK HOW TO CODE THE FUNCTION diff
                byte[] difference = new byte[3];
                for (int j = 0; j < difference.length; j++)
                    difference[j] = (byte) (image[i][j] - previousPixel[j]);//SEE HOW TO CALCULATE DIFFERENCE ????
                encodedData.add(qoiOpDiff(difference));
                previousPixel = image[i];
                continue;
            }


            //QOI_OP_LUMA Block step 4
            if (image[i][3] == previousPixel[3] && luma(image[i], previousPixel)) {
                encodedData.add(qoiOpLuma(getLumaDiff(image[i], previousPixel)));
                previousPixel = image[i];
                continue;
            }

            //QOI_OP_RGB Block step 5
            if (image[i][3] == previousPixel[3]) {
                encodedData.add(qoiOpRGB(image[i]));
                previousPixel = image[i];
                continue;
            }

            encodedData.add(qoiOpRGBA(image[i]));

            previousPixel = image[i];
        }
        */

        // turn encoded data into byte[]
        byte[][] result = new byte[encodedData.size()][];
        int i = 0;
        for (byte[] data : encodedData) {
            result[i] = data;
            i++;

            System.out.println(Arrays.toString(data));
        }

        Hexdump.hexdump(ArrayUtils.concat(result));

        return ArrayUtils.concat(result);
    }

    // version 2
    /*public static boolean diff (byte[] current, byte[] previous, byte[] difference){
        getDiff(current, previous, difference);
        for (byte valueDifference : difference) {
            if (valueDifference > 1 || valueDifference < -2) return false;
        }
        return true;
    }*/

    // version 1
    public static boolean diff(byte[] current, byte[]previous){
        for (int i = 0; i < current.length-1; i++) {
            if (current[i] - previous[i] > 1 || current[i] - previous[i] < -2) return false;
        }
        return true;
    }

    // version 2
    /*public static void getDiff(byte[] current, byte[] previous, byte[] difference) {
        for (int i = 0; i < 3; i++){
            difference[i] = (byte)(current[i] - previous[i]);
        }
    }*/

    // version 2
    /*private static boolean luma(byte[] current, byte[] previous, byte[] difference){
        getDiff(current, previous, difference);

        if (difference[1] >= 32 || difference[1] <= -32) return false;
        for (int i = 0; i < 3; i += 2){
            if (difference[i] - difference[1] >= 8 || difference[i] - difference[1] <= -9) return false;
        }
        return true;
    }*/

    // version 1
    public static boolean luma (byte[] current, byte[] previous){
        byte[] diff = getLumaDiff(current, previous);

        if (diff[1] >= 32 || diff[1] <= -32) return false;
        if (diff[0] - diff[1] >= 8 || diff[0] - diff[1] <= -9) return false;
        if (diff[2] - diff[1] >= 8 || diff[2] - diff[1] <= -9) return false;

        return true;
    }

    // version 1
    public static byte[] getLumaDiff(byte[] current, byte[] previous) {
        byte green_diff = (byte)(current[1] - previous[1]);
        byte red_green_diff = (byte)(current[0] - previous[0]);
        byte blue_green_diff = (byte)(current[2] - previous[2]);

        return new byte[] {red_green_diff, green_diff, blue_green_diff};
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image){
        assert image!=null;

        byte[] encodedData = encodeData(ArrayUtils.imageToChannels(image.data()));

        return ArrayUtils.concat(qoiHeader(image), encodedData, QOISpecification.QOI_EOF);
    }
}