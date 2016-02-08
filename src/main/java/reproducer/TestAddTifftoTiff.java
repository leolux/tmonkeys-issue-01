package reproducer;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.exif.EXIFReader;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageMetadata;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageWriter;


/**
 * Demonstrates an issue where unwanted artifacts occur when a tiff written by TwelvekMonkeys gets
 * added to another tiff. The issue does not occur when both tiffs have not been written by
 * TwelvekMonkeys previously.
 * 
 * @author Mathias
 *
 */
public class TestAddTifftoTiff {

  public static void main(String[] args) throws Exception {
    ImageIO.scanForPlugins();
    ImageIO.setUseCache(false);
    TestAddTifftoTiff t = new TestAddTifftoTiff();
    t.runWithBug();
    t.runWithoutBug();
    System.out.println("Finished!");
  }

  private void runWithBug() throws Exception {
    File file1 = loadFile("dina4.tiff");

    File prepareFile2 = loadFile("img.tiff");
    File file2 = writeFileWithTwelvemonkeys(prepareFile2); // if you leave this step out everything
                                                           // works! So this method "introduces" the
                                                           // issue.

    File outFile = new File("issue-01-withbug.tiff");
    testWriteTifftoTiff(file1, file2, outFile);
  }

  private void runWithoutBug() throws Exception {
    File file1 = loadFile("dina4.tiff");

    File file2 = loadFile("img.tiff");

    File outFile = new File("issue-01-withoutbug.tiff");
    testWriteTifftoTiff(file1, file2, outFile);
  }

  private File writeFileWithTwelvemonkeys(File prepareFile2) throws Exception {
    BufferedImage bufferImage = ImageIO.read(prepareFile2);
    // Write tiff
    File out = new File("_out.tiff");
    String format = "tiff";
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
    TIFFImageWriter writer = (TIFFImageWriter) writers.next();
    try {
      ImageOutputStream output = ImageIO.createImageOutputStream(out);

      try {
        writer.setOutput(output);
        ImageWriteParam param = writer.getDefaultWriteParam();
        ImageInputStream input = ImageIO.createImageInputStream(prepareFile2);
        Directory ifd = new EXIFReader().read(input);
        TIFFImageMetadata metadata = new TIFFImageMetadata(ifd);
        IIOImage iioimage = new IIOImage(bufferImage, null, metadata);
        writer.write(metadata, iioimage, param);
        return out;
      } finally {
        output.close();
      }
    } finally {
      writer.dispose();
    }
  }

  private void testWriteTifftoTiff(File inFile1, File inFile2, File outFile) throws Exception {
    BufferedImage bufferImage = ImageIO.read(inFile1);
    Graphics2D g = bufferImage.createGraphics();
    g.setComposite(AlphaComposite.Src);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
        RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

    // Add tiff to tiff
    BufferedImage itemImg = ImageIO.read(inFile2);
    int xLeft = 100;
    int xTop = 200;

    g.drawImage(itemImg, xLeft, xTop, null);
    g.dispose();

    // Write tiff
    String format = "tiff";
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
    TIFFImageWriter writer = (TIFFImageWriter) writers.next();
    try {
      ImageOutputStream output = ImageIO.createImageOutputStream(outFile);

      try {
        writer.setOutput(output);
        ImageWriteParam param = writer.getDefaultWriteParam();
        ImageInputStream input = ImageIO.createImageInputStream(inFile1);
        Directory ifd = new EXIFReader().read(input);
        TIFFImageMetadata metadata = new TIFFImageMetadata(ifd);
        IIOImage iioimage = new IIOImage(bufferImage, null, metadata);
        writer.write(metadata, iioimage, param);
      } finally {
        output.close();
      }
    } finally {
      writer.dispose();
    }
  }

  private File loadFile(String name) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(name).toURI());
  }
}
