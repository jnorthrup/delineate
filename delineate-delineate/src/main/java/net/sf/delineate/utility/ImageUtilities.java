/*
 * ImageUtilities.java
 *
 * Copyright (C) 2003 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.delineate.utility;

import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.color.promotion.PromotionRGB24;
import net.sourceforge.jiu.color.reduction.RGBToGrayConversion;
import net.sourceforge.jiu.color.reduction.ReduceToBilevelThreshold;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Image helper methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class ImageUtilities {

    private static RGBToGrayConversion rgbConverter = new RGBToGrayConversion();
    private static ReduceToBilevelThreshold greyConverter = new ReduceToBilevelThreshold();

    /**
     * True if file is in bitmap format.
     */
    public static boolean inBmpFormat(File file) {
        String extension = FileUtilities.getExtension(file);
        return extension.equalsIgnoreCase("bmp");
    }

    /**
     * True if file is in PNM format.
     */
    public static boolean inPnmFormat(File file) {
        String extension = FileUtilities.getExtension(file);
        return extension.equalsIgnoreCase("pnm") // any file
                || extension.equalsIgnoreCase("pbm") // black and white
                || extension.equalsIgnoreCase("pgm") // grey scale
                || extension.equalsIgnoreCase("ppm"); // other palette
    }

    /**
     * True if file is in PBM format.
     */
    public static boolean inPbmFormat(File file) {
        String extension = FileUtilities.getExtension(file);
        return extension.equalsIgnoreCase("pbm"); // black and white
    }

    /**
     * @return dimension of image contained in file.
     */
    public static Dimension getDimension(File file) throws IOException, OperationFailedException {
        PixelImage image = loadPnm(file);
        return new Dimension(image.getWidth(), image.getHeight());
    }

    /**
     * Converts image contained in file to PNM format.
     *
     * @param file containing image to be converted
     * @return file containing the converted image in PBM, PGM or PPM format.
     */
    public static File convertToPnm(File file) throws IOException, OperationFailedException {
        PixelImage pixelImage = loadViaToolkitOrCodecs(file);

        if(pixelImage == null) {
            throw new OperationFailedException("Unsupported input file format.");
        } else {
            return saveAsPnm(pixelImage, file.getName());
        }
    }

    /**
     * Converts image contained in file to black and white PBM format,
     * using the given darkness threshold.
     *
     * @param file containing image to be converted
     * @param thresholdPercent brightness level between 0 and 100, colors
     *                         brighter than threshold are converted to white,
     *                         those below to black.
     * @return file containing the converted image in PBM format.
     * @throws IOException
     * @throws OperationFailedException
     */
    public static File convertToPbm(File file, int thresholdPercent) throws IOException, OperationFailedException {
        if (inPbmFormat(file)) {
            return file;
        }

        boolean needsConversion = !inPnmFormat(file);
        if (needsConversion) {
            file = convertToPnm(file);
        }

        GrayIntegerImage greyImage = convertRgbToGrey(promoteToRgb(loadPnm(file)));
        PixelImage image = convertGreyToBilevel(greyImage, thresholdPercent);

        File outputFile = saveAsPnm(image, file.getName());
        if (needsConversion) {
            file.delete();
        }

        return outputFile;
    }

    private static File saveAsPnm(PixelImage pixelImage, String namePrefix) throws IOException, OperationFailedException {
        PNMCodec codec = new PNMCodec();
        String extension = codec.suggestFileExtension(pixelImage);
        File outputFile = new File(FileUtilities.getTempDir(), namePrefix + extension);
        codec.setImage(pixelImage);
        codec.setFile(outputFile, CodecMode.SAVE);
        codec.process();
        codec.close();
        codec = null;
        return outputFile;
    }

    private static PixelImage loadPnm(File inputFile) throws IOException, OperationFailedException {
        PNMCodec codec = new PNMCodec();
        codec.setFile(inputFile, CodecMode.LOAD);
        codec.process();
        codec.close();
        PixelImage image = codec.getImage();
        codec = null;
        return image;
    }

    private static RGB24Image promoteToRgb(PixelImage image) throws MissingParameterException, WrongParameterException {
        if (!(image instanceof RGB24Image)) {
            PromotionRGB24 promoter = new PromotionRGB24();
            promoter.setInputImage(image);
            promoter.process();
        }

        return (RGB24Image) image;
    }

    private static GrayIntegerImage convertRgbToGrey(RGB24Image image) throws OperationFailedException {
        rgbConverter.setInputImage(image);
//        rgbConverter.setColorWeights(0.33f, 0.33f, 0.33f);
        rgbConverter.process();
        GrayIntegerImage outputImage = (GrayIntegerImage) rgbConverter.getOutputImage();
        rgbConverter.setOutputImage(null);

        return outputImage;
    }

    private static BilevelImage convertGreyToBilevel(GrayIntegerImage image, int thresholdPercent) throws OperationFailedException {
        greyConverter.setInputImage(image);
        greyConverter.setThreshold(image.getMaxSample(0) * thresholdPercent / 100);
        greyConverter.process();
        BilevelImage outputImage = (BilevelImage) greyConverter.getOutputImage();
        greyConverter.setOutputImage(null);

        return outputImage;
    }

    private static PixelImage loadViaToolkitOrCodecs(File file) throws IOException, OperationFailedException {
        PixelImage result = ToolkitLoader.loadAsRgb24Image(file.getPath());
        if(result == null) {
            result = load(file);
        }

        return result;
    }

    public static PixelImage load(File file) throws IOException, OperationFailedException {

        for(int i = 0; i < ImageLoader.getNumCodecs(); i++) {
            PixelImage result = null;
            try {
                ImageCodec codec = ImageLoader.createCodec(i);
                codec.setFile(file, CodecMode.LOAD);
                codec.process();
                result = codec.getImage();
                if(result != null) {
                    return result;
                }
            } catch(WrongFileFormatException wffe) {
                // ignore
            } catch(OperationFailedException e) {
                throw e;
            }
        }
        return null;
    }
}
