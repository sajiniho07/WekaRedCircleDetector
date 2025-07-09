# WekaRedCircleDetector

## Overview
WekaRedCircleDetector is a Java-based project that combines OpenCV and Weka to detect and classify red circles in images. It uses OpenCV for image processing to identify circles and extract features, and Weka to train a machine learning model (J48 decision tree) for classifying circles as red or non-red. The project includes functionality to generate a training model and detect red circles in new images, drawing green outlines around detected red circles.

## Features
- **Circle Detection**: Utilizes OpenCV's HoughCircles to detect circles in images.
- **Feature Extraction**: Extracts features such as circle center coordinates (x_center, y_center), radius, and mean RGB colors.
- **Model Training**: Generates a Weka dataset in ARFF format and trains a J48 decision tree model to classify circles as red or non-red.
- **Prediction**: Uses the trained model to predict red circles in new images and visualizes results by drawing green circles around detected red circles.
- **Output**: Saves processed images with annotated red circle counts and stores the trained model and ARFF dataset.

## Prerequisites
- **Java**: JDK 8 or higher
- **OpenCV**: Version 4.5.0 or higher (ensure the native library is loaded)
- **Weka**: Version 3.8 or higher
- **Spring Framework**: For dependency injection and service management
- **Maven**: For dependency management

## Installation
1. **Clone the Repository**:
   ```bash
   git clone ...
   cd WekaRedCircleDetector
   ```

2. **Install OpenCV**:
   - Download and install OpenCV for Java from [OpenCV.org](https://opencv.org/releases/).
   - Set up the native library path (e.g., `-Djava.library.path=/path/to/opencv/lib`).

3. **Add Weka Dependency**:
   Add the following to your `pom.xml`:
   ```xml
   <dependency>
       <groupId>nz.ac.waikato.cms.weka</groupId>
       <artifactId>weka-stable</artifactId>
       <version>3.8.6</version>
   </dependency>
   ```

4. **Build the Project**:
   ```bash
   mvn clean install
   ```

## Usage
1. **Generate a Model**:
   - Use the `generateModel(MultipartFile file)` method to process an image and create a training dataset and model.
   - Input: An image file (e.g., PNG or JPG) containing circles.
   - Output: 
     - `red_circle_dataset.arff`: The generated Weka dataset.
     - `red_circle_detection.model`: The trained J48 model.

   Example:
   ```java
   ImageProcessingService service = new ImageProcessingService();
   service.generateModel(multipartFile);
   ```

2. **Detect Red Circles**:
   - Use the `detectRedCircles(MultipartFile file)` method to detect red circles in a new image using the trained model.
   - Output: A processed image (`predicted_image.jpg`) with green circles drawn around detected red circles and a text annotation showing the count.

   Example:
   ```java
   service.detectRedCircles(multipartFile);
   ```

## Code Structure
- **Package**: `org.example.wekaredcircledetector.service`
- **Main Class**: `ImageProcessingService`
  - `generateModel(MultipartFile file)`: Processes an image, extracts features, creates an ARFF dataset, and trains a J48 model.
  - `detectRedCircles(MultipartFile file)`: Uses the trained model to detect and highlight red circles in a new image.
  - Helper methods: Feature extraction, dataset creation, and image processing utilities.

## Example Workflow
1. Prepare an image with circles (some red, some non-red).
2. Call `generateModel` to create the training dataset and model.
3. Use `detectRedCircles` on a new image to detect red circles.
4. Check the output image (`predicted_image.jpg`) for results.

## Notes
- The project uses a simple color-based heuristic (`red > 150 && green < 60 && blue < 60`) for initial labeling during training. For better accuracy, provide annotated training data.
- The model can be retrained with different algorithms (e.g., SMO) by modifying the `generateModel` method.
- Ensure sufficient memory for processing large images.

## Future Improvements
- Add support for other Weka classifiers (e.g., SMO, RandomForest).
- Improve feature extraction with additional attributes (e.g., texture, edge strength).
- Implement a web interface for easier image uploads and visualization.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.