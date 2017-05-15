package jp.topse.bigdata.wf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVReader;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class ConvertToARFF {

    static final int LINES_OF_HEADER = 6;
    static final String INPUT_DIR = "./src/main/resources";
    static final String OUTPUT_PATH = "./src/main/resources/weather.arff";
    
    public static void main(String[] args) {
        ConvertToARFF app = new ConvertToARFF();

//        String[] csvList = { "2001.csv" };
        String[] csvList = { "2001.csv", "2002.csv", "2003.csv", "2004.csv", "2005.csv",
                "2006.csv", "2007.csv", "2008.csv", "2009.csv", "2010.csv",
                "2011.csv", "2012.csv", "2013.csv", "2014.csv", "2015.csv",
                "2016.csv"
        };

        app.convert(INPUT_DIR, csvList, OUTPUT_PATH);
    }
    
    private void convert(String inputDir, String[] csvList, String outputPath) {
        Instances data = prepareInstances();

        for (String csv : csvList) {
            File file = new File(inputDir + File.separator + csv);
            try {
                CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "SJIS"));
                for (int i = 0; i < LINES_OF_HEADER; ++i) {
                    reader.readNext();
                }

                String[] values = null;
                while ((values = reader.readNext()) != null) {
                    appendInstance(data, values);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(data);
            arffSaver.setFile(new File(outputPath));
            arffSaver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Instances prepareInstances() {
        FastVector attributes = new FastVector();

        attributes.addElement(new Attribute("date", (FastVector)null));

        // 最高気温(℃)
        attributes.addElement(new Attribute("highest temperature"));
        // 最低気温(℃)
        attributes.addElement(new Attribute("lowest temperature"));

        // TODO: Add more elements if needed.

        FastVector weatherValues = new FastVector();
        weatherValues.addElement("快晴");
        weatherValues.addElement("晴");
        weatherValues.addElement("曇");
        weatherValues.addElement("雨");
        weatherValues.addElement("不明");

        // TODO: Add more weather if needed.

        attributes.addElement(new Attribute("weather", weatherValues));

        Instances data = new Instances("Weather", attributes, 0);

        return data;
    }

    private void appendInstance(Instances data, String[] csvValues) {
        double[] values = new double[data.numAttributes()];
        values[0] = data.attribute(0).addStringValue(csvValues[0]);
        values[1] = Double.parseDouble(csvValues[1]);
        values[2] = Double.parseDouble(csvValues[4]);

        String weather = filterWeather(csvValues[56]);
        values[3] = data.attribute(3).indexOfValue(weather);

        data.add(new Instance(1.0, values));
    }

    private String filterWeather(String weather) {
        if ("快晴".equals(weather)) {
            return "快晴";
        }

        Pattern p1 = Pattern.compile("晴.*");
        Matcher m1 = p1.matcher(weather);
        if (m1.find()) {
            return "晴";
        }

        Pattern p2 = Pattern.compile("曇.*");
        Matcher m2 = p2.matcher(weather);
        if (m2.find()) {
            return "曇";
        }

        Pattern p3 = Pattern.compile("雨.*");
        Matcher m3 = p3.matcher(weather);
        if (m3.find()) {
            return "雨";
        }

        return "不明";
    }


}
