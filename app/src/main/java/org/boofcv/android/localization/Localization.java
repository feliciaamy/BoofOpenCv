/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.boofcv.android.localization;

import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.alg.feature.detect.template.TemplateMatchingIntensity;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.misc.PixelMath;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.io.UtilIO;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Example of how to find objects inside an image using template matching.  Template matching works
 * well when there is little noise in the image and the object's appearance is known and static.  It can
 * also be very slow to compute, depending on the image and template size.
 *
 * @author Peter Abeles
 */
public class Localization {

    /**
     * Demonstrates how to search for matches of a template inside an image
     *
     * @param image           Image being searched
     * @param template        Template being looked for
     * @param mask            Mask which determines the weight of each template pixel in the match score
     * @param expectedMatches Number of expected matches it hopes to find
     * @return List of match location and scores
     */
    private static List<Match> findMatches(GrayF32 image, GrayF32 template, GrayF32 mask,
                                           int expectedMatches) {
        // create template matcher.
        TemplateMatching<GrayF32> matcher =
                FactoryTemplateMatching.createMatcher(TemplateScoreType.NCC, GrayF32.class);

        // Find the points which match the template the best
        matcher.setImage(image);
        matcher.setTemplate(template, mask, expectedMatches);
        matcher.process();

        return matcher.getResults().toList();

    }

    /**
     * Computes the template match intensity image and displays the results. Brighter intensity indicates
     * a better match to the template.
     */
    public static void showMatchIntensity(GrayF32 image, GrayF32 template, GrayF32 mask) {

        // create algorithm for computing intensity image
        TemplateMatchingIntensity<GrayF32> matchIntensity =
                FactoryTemplateMatching.createIntensity(TemplateScoreType.NCC, GrayF32.class);

        // apply the template to the image
        matchIntensity.setInputImage(image);
        matchIntensity.process(template, mask);

        // get the results
        GrayF32 intensity = matchIntensity.getIntensity();
        // adjust the intensity image so that white indicates a good match and black a poor match
        // the scale is kept linear to highlight how ambiguous the solution is
        float min = ImageStatistics.min(intensity);
        float max = ImageStatistics.max(intensity);
        float range = max - min;
        System.out.println("Min: " + min + ", Max: " + max + ", Range: " + range);
        PixelMath.plus(intensity, -min, intensity);
        PixelMath.divide(intensity, range, intensity);
        PixelMath.multiply(intensity, 255.0f, intensity);

//        BufferedImage output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
//        VisualizeImageData.grayMagnitude(intensity, output, -1);
//        ShowImages.showWindow(output, "Match Intensity", true);
    }

    public static void main(String args[]) {

        // Load image and templates
        String directory = UtilIO.pathExample("template");
        GrayF32 image = UtilImageIO.loadImage(directory, "m_75_1.jpg", GrayF32.class);
        GrayF32 image1 = UtilImageIO.loadImage(directory, "m_75_2.jpg", GrayF32.class);
        GrayF32 image2 = UtilImageIO.loadImage(directory, "m_75_3.jpg", GrayF32.class);
        GrayF32 image3 = UtilImageIO.loadImage(directory, "m_75_4.jpg", GrayF32.class);
        GrayF32 image4 = UtilImageIO.loadImage(directory, "m_75_5.jpg", GrayF32.class);
        GrayF32[] images = {image, image1, image2, image3, image4};
//		Boxes used:
        GrayF32 template = UtilImageIO.loadImage(directory, "template_75.jpg", GrayF32.class);
        GrayF32 template2 = UtilImageIO.loadImage(directory, "template_75_1.jpg", GrayF32.class);
        GrayF32 template3 = UtilImageIO.loadImage(directory, "template_75_2.jpg", GrayF32.class);
        GrayF32 template4 = UtilImageIO.loadImage(directory, "template_75_3.jpg", GrayF32.class);
        GrayF32 template5 = UtilImageIO.loadImage(directory, "template_75_4.jpg", GrayF32.class);
        GrayF32 template6 = UtilImageIO.loadImage(directory, "template_75_5.jpg", GrayF32.class);
        // create output image to show results
        showMatchIntensity(image, template, null);
        int i = 0;
        for (GrayF32 g : images) {
            for (int th = 0; th <= 500000; th += 50000) {
                System.out.println(th);
//                BufferedImage output = new BufferedImage(g.width, g.height, BufferedImage.TYPE_INT_BGR);
//                ConvertBufferedImage.convertTo(g, output);
//                Graphics2D g2 = output.createGraphics();
                List<Match> allMatches = new ArrayList<>();

                int expected = 15;

//            allMatches = findMatches(g, box1, mask1, expected);
//            allMatches.addAll(findMatches(g, box4, mask4, expected));
//            allMatches.addAll(findMatches(g, box5, null, expected));
//            allMatches.addAll(findMatches(g, box6, null, expected));
//            allMatches.addAll(findMatches(g, box12, null, expected));
//            allMatches.addAll(findMatches(g, box13, null, expected));

                allMatches = findMatches(g, template, null, expected);
                allMatches.addAll(findMatches(g, template2, null, expected));
                allMatches.addAll(findMatches(g, template3, null, expected));
                allMatches.addAll(findMatches(g, template4, null, expected));
                allMatches.addAll(findMatches(g, template5, null, expected));
                allMatches.addAll(findMatches(g, template6, null, expected));
                allMatches = eliminateMatches(allMatches, th);
                int width = (template.getWidth() + template2.getWidth()) / 2;
                int height = (template.getHeight() + template2.getHeight()) / 2;
                int image_height = g.height;
//                drawRectangles(g2, allMatches, width, height, image_height);

//                ShowImages.showWindow(output, "Found Matches", true);

                File outputfile = new File("shelf_" + i + "_75_" + th + ".jpg");
//                try {
//                    ImageIO.write(output, "jpg", new File("data/boxDetection/Controlled_75_NCC/" + th + "/" + outputfile.getName()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            i++;
        }

        System.out.println("DONE");
    }

    private static List<Match> eliminateMatches(List<Match> matches, int treshold) {
//        System.out.println(matches + " " + "Size: " + matches.size());
        List<Match> result = new ArrayList<Match>();
        Queue<Match> queue = new LinkedList<>(matches);
        while (!queue.isEmpty()) {
            boolean match = false;
            Match m = queue.poll();
            if (m.score < -treshold)  // 2900000
                continue;
//            System.out.println(m.toString());
            Queue tempQ = new LinkedList();
            while (!queue.isEmpty()) {
//                System.out.println("Size Q: " + queue.size());
                Match temp = queue.poll();
                if (temp.equals(m)) {
                    match = true;
//                    System.out.println("Match: " + m.toString() + " & " + temp.toString());
                    if (temp.score > m.score) {
                        m = temp;
                    }
                } else {
                    tempQ.add(temp);
                }
            }
            queue = tempQ;
            if (match) {
                result.add(m);
            }
        }
        System.out.println("RESULT SIZE: " + result.size());
        return result;
    }

    /**
     * Helper function will is finds matches and displays the results as colored rectangles
     */
    private static void drawRectangles(List<Match> found, int width, int height, int image_height) {
        System.out.println(image_height);
        int r = 2;
        int w = width + 2 * r;
        int h = height + 2 * r;

        List<Integer> xs = new ArrayList<Integer>();
        List<Integer> y_min = new ArrayList<Integer>();
        int y_max = 0;
        for (Match m : found) {
            System.out.println("Match " + m.x + " " + m.y + "    score " + m.score);
            int x0 = m.x - r;
            int y0 = m.y - r;
            int x1 = x0 + w;
            int y1 = y0 + h;

//            g2.setColor(Color.RED);
//            g2.setStroke(new BasicStroke(2));
//            g2.drawString(m.x + " " + m.y, m.x, m.y + 10);
//            g2.drawLine(x0, y0, x1, y0);
//            g2.drawLine(x1, y0, x1, y1);
//            g2.drawLine(x1, y1, x0, y1);
//            g2.drawLine(x0, y1, x0, y0);

            if (xs.isEmpty()) {
                xs.add(m.x);
                y_min.add(m.y);
                y_max = m.y;
            } else {
                boolean done = false;
                for (int i = 0; i < xs.size(); i++) {
                    int x = xs.get(i);
                    if (Math.abs(m.x - x) < 10) {
                        xs.set(i, (x + m.x) / 2);
                        done = true;
                        if (y_min.get(i) > m.y) {
                            y_min.set(i, m.y);
                        }
                        break;
                    }
                }
                if (!done) {
                    xs.add(m.x);
                    y_min.add(m.y);
                }
                if (y_max < m.y) {
                    y_max = m.y;
                }
            }
            System.out.println(xs.toString() + " " + xs.size());
            System.out.println(y_min.toString());
            System.out.println(y_max);
        }
        DecimalFormat df = new DecimalFormat("#.00");
        for (int i = 0; i < xs.size(); i++) {
            int x0 = xs.get(i) - r;
            int y0 = y_min.get(i) - r;
            int x1 = x0 + w;
            int y1 = y_max + h;
//            g2.setColor(Color.WHITE);
//            g2.drawLine(x0, y0, x1, y0);
//            g2.drawLine(x1, y0, x1, y1);
//            g2.drawLine(x1, y1, x0, y1);
//            g2.drawLine(x0, y1, x0, y0);
//            String angleFormated = df.format((y_max + h - y_min.get(i)) / (double) (y_max + h) * 100);
//            g2.drawString(angleFormated + "%", xs.get(i), y_min.get(i) - 10);
        }
    }
}

// Search for the cursor in the image.  For demonstration purposes it has been pasted 3 times
//		drawRectangles(g2, image, templateCursor, maskCursor, 3);
// show match intensity image for this template
//		showMatchIntensity(image, templateCursor, maskCursor);
//		GrayF32 image = UtilImageIO.loadImage(directory ,"desktop.png", GrayF32.class);
//		GrayF32 templateCursor = UtilImageIO.loadImage(directory , "cursor.png", GrayF32.class);
//		GrayF32 maskCursor = UtilImageIO.loadImage(directory , "cursor_mask.png", GrayF32.class);
//		GrayF32 templatePaint = UtilImageIO.loadImage(directory , "paint.png", GrayF32.class);

//	GrayF32 box2 = UtilImageIO.loadImage(directory , "box2.jpg", GrayF32.class);
//	GrayF32 box3 = UtilImageIO.loadImage(directory , "box3.jpg", GrayF32.class);
//	GrayF32 box7 = UtilImageIO.loadImage(directory , "box7.jpg", GrayF32.class);
//	GrayF32 box8 = UtilImageIO.loadImage(directory , "box8.jpg", GrayF32.class);
//	GrayF32 box9 = UtilImageIO.loadImage(directory , "box9.jpg", GrayF32.class);
//	GrayF32 label1 = UtilImageIO.loadImage(directory , "label1.jpg", GrayF32.class);
//	GrayF32 box10 = UtilImageIO.loadImage(directory , "box10.jpg", GrayF32.class);
//	GrayF32 box11 = UtilImageIO.loadImage(directory , "box11.jpg", GrayF32.class);

//	GrayF32 maskLabel = UtilImageIO.loadImage(directory , "maskLabel.jpg", GrayF32.class);
