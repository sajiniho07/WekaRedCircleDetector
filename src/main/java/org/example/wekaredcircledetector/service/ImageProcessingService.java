package org.example.wekaredcircledetector.service;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.*;
import weka.core.converters.ArffSaver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ImageProcessingService {

    public void generateModel(MultipartFile file) throws Exception {
        BufferedImage inputImage = ImageIO.read(file.getInputStream());
        Mat image = bufferedImageToMat(inputImage);
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat circlesMat = new Mat();
        Imgproc.HoughCircles(gray, circlesMat, Imgproc.HOUGH_GRADIENT_ALT, 1, 10, 1.5, 0.7, 10, 25);

        List<Attribute> attributes = createAttributes();
        Instances dataset = initializeInstances(attributes);
        if (!circlesMat.empty()) {
            int cols = circlesMat.cols();
            for (int i = 0; i < cols; i++) {
                double[] circle = circlesMat.get(0, i);
                addInstanceToData(image, dataset, circle);
            }
        }
        generateModel(dataset);
    }

    private void addInstanceToData(Mat image, Instances data, double[] circle) {
        int numAttributes = data.numAttributes();
        Instance instance = new DenseInstance(numAttributes);
        instance.setDataset(data);
        int xCent = (int) circle[0];
        int yCent = (int) circle[1];
        int radius = (int) circle[2];
        Scalar meanColor = getMeanColor(image, xCent, yCent);
        boolean isRed = isRedCircle(meanColor);

        instance.setValue(0, yCent);
        instance.setValue(1, xCent);
        instance.setValue(2, radius);
        instance.setValue(3, meanColor.val[0]); // blue
        instance.setValue(4, meanColor.val[1]); // green
        instance.setValue(5, meanColor.val[2]); // red
        instance.setValue(6, isRed ? "true" : "false");

        data.add(instance);
    }

    private List<Attribute> createAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("y_center"));
        attributes.add(new Attribute("x_center"));
        attributes.add(new Attribute("radius"));
        attributes.add(new Attribute("blue_mean"));
        attributes.add(new Attribute("green_mean"));
        attributes.add(new Attribute("red_mean"));
        attributes.add(new Attribute("red_circle", Arrays.asList("true", "false")));
        return attributes;
    }

    private Instances initializeInstances(List<Attribute> attributes) {
        Instances data = new Instances("RedCircleDatasetTrain", new ArrayList<>(attributes), 0);
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    private void generateModel(Instances dataset) {
        try {
            J48 classifier = new J48();
            classifier.buildClassifier(dataset);

            File arffFile = new File(System.getProperty("user.dir") + "/red_circle_dataset.arff");
            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataset);
            saver.setFile(arffFile);
            saver.writeBatch();

            String modelFilePath = System.getProperty("user.dir") + "/red_circle_detection.model";
            SerializationHelper.write(modelFilePath, classifier);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private Scalar getMeanColor(Mat image, int xCent, int yCent) {
        int startY = Math.max(0, yCent - 2);
        int endY = Math.min(image.rows(), yCent + 1);
        int startX = Math.max(0, xCent - 2);
        int endX = Math.min(image.cols(), xCent + 1);
        Mat littleImg = image.submat(startY, endY, startX, endX);
        return Core.mean(littleImg);
    }

    private boolean isRedCircle(Scalar mean) {
        double red = mean.val[2];
        double green = mean.val[1];
        double blue = mean.val[0];
        return red > 150 && green < 60 && blue < 60;
    }
    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((java.awt.image.DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public void detectRedCircles(MultipartFile file) throws Exception {
        BufferedImage inputImage = ImageIO.read(file.getInputStream());
        Mat image = bufferedImageToMat(inputImage);
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat circlesMat = new Mat();
        Imgproc.HoughCircles(gray, circlesMat, Imgproc.HOUGH_GRADIENT_ALT, 1, 10, 1.5, 0.7, 10, 25);

        Classifier classifier = (Classifier) SerializationHelper.read(System.getProperty("user.dir") + "/red_circle_detection.model");

        List<Attribute> attributes = createAttributes();
        Instances dataset = new Instances("RedCircleDatasetTest", new ArrayList<>(attributes), 0);
        dataset.setClassIndex(attributes.size() - 1);

        int redCircleCount = 0;

        if (!circlesMat.empty()) {
            int cols = circlesMat.cols();
            for (int i = 0; i < cols; i++) {
                double[] circle = circlesMat.get(0, i);
                int xCent = (int) circle[0];
                int yCent = (int) circle[1];
                int radius = (int) circle[2];

                Scalar meanColor = getMeanColor(image, xCent, yCent);
                Instance instance = new DenseInstance(dataset.numAttributes());
                instance.setDataset(dataset);

                instance.setValue(0, yCent);
                instance.setValue(1, xCent);
                instance.setValue(2, radius);
                instance.setValue(3, meanColor.val[0]); // blue
                instance.setValue(4, meanColor.val[1]); // green
                instance.setValue(5, meanColor.val[2]); // red

                double prediction = classifier.classifyInstance(instance);
                String classLabel = dataset.classAttribute().value((int) prediction);

                if (classLabel.equals("true")) {
                    redCircleCount++;
                    Imgproc.circle(image, new Point(xCent, yCent), radius, new Scalar(0, 255, 0), 2);
                }
            }

            Imgproc.putText(image, "Red circle count: " + redCircleCount, new Point(20, 20),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 1);
        }

        String outputPath = System.getProperty("user.dir") + "/predicted_image.jpg";
        Imgcodecs.imwrite(outputPath, image);
    }
}