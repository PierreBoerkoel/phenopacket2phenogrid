package com.boerkoel.phenopacket_phenogrid_converter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Hello world!
 *
 */
public class App 
{   
    public static String inputFolder;
    public static String outputFolder;

    public static void main( String[] args ) {

        inputFolder = "/Users/pierreboerkoel/Programming/phenopacket_phenogrid_converter/data_clean/negative/input/";
        outputFolder = "/Users/pierreboerkoel/Programming/phenopacket_phenogrid_converter/data_clean/negative/output/";

        File folder = new File(inputFolder);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json")) {
                System.out.println(file.getName() + "\n");
            } else {
                continue;
            }
        
            JSONParser parser = new JSONParser();
            StringBuilder htmlHpoTerms = new StringBuilder();

            try {
                Object js = parser.parse(new FileReader(inputFolder + file.getName()));
                JSONObject json = (JSONObject) js;
                
                JSONArray ja = (JSONArray) json.get("phenotypicFeatures");

                for (int i = 0; i < ja.size(); i++) {
                    JSONObject phenotypeFeature = (JSONObject) ja.get(i);
                    htmlHpoTerms = addHpoTerm(phenotypeFeature, htmlHpoTerms);
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }
            writePhenogridHtml(htmlHpoTerms.toString(), FilenameUtils.removeExtension(file.getName()));
        }
    }

    public static StringBuilder addHpoTerm(JSONObject hpoTerm, StringBuilder hpoTerms) {
        JSONObject phenotypeType = (JSONObject) hpoTerm.get("type");
        String phenotypeId = (String) phenotypeType.get("id");
        String phenotypeTerm = (String) phenotypeType.get("label");

        hpoTerms.append(
            "\t\t{\n" +
                "\t\t\t\"id\": " + "\"" + phenotypeId + "\"" + ",\n" +
                "\t\t\t\"term\": " + "\"" + phenotypeTerm + "\"" + "\n" +
            "\t\t},\n"
        );

        return hpoTerms;
    }

    public static void writePhenogridHtml(String yAxis, String fileName) {
        if (yAxis != null && yAxis.length() > 0 && yAxis.charAt(yAxis.length() - 2) == ',') {
            yAxis = yAxis.substring(0, yAxis.length() - 2) + "\n";
            System.out.println(yAxis);
        }

        try {
            File phenogrid = new File(outputFolder + fileName + ".html");
            BufferedWriter bPhenogrid = new BufferedWriter(new FileWriter(phenogrid));
            bPhenogrid.write (
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<title>Monarch Phenotype Grid Widget</title>\n" +
                "\n" +
                "<script src=\"node_modules/phenogrid/config/phenogrid_config.js\"></script>\n" +
                "<script src=\"node_modules/phenogrid/dist/phenogrid-bundle.js\"></script>\n" +
                "\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"node_modules/phenogrid/dist/phenogrid-bundle.css\">\n" +
                "\n" +
                "<script>\n" +
                "var data = {\n" +
                    "\t\"title\": \"Diseases, Mouse and Fish models\",\n" +
                    "\t\"xAxis\": [\n" +
                        "\t\t{\n" +
                            "\t\t\t\"groupId\": \"9606\",\n" +
                            "\t\t\t\"groupName\": \"Homo sapiens\"\n" +
                        "\t\t},\n" +
                        "\t\t{\n" +
                            "\t\t\t\"groupId\": \"10090\",\n" +
                            "\t\t\t\"groupName\": \"Mus musculus\"\n" +
                        "\t\t},\n" +
                        "\t\t{\n" +
                            "\t\t\t\"groupId\": \"7955\",\n" +
                            "\t\t\t\"groupName\": \"Danio rerio\"\n" +
                        "\t\t}\n" +
                    "\t],\n" +
                    "\t\"yAxis\": [\n" +
                        yAxis + "\n" +
                    "\t]\n" +
                "};\n" +
                "\n" +
                "window.onload = function() {\n" +
                "\t// There are three species that are loaded and each of them has simsearch matches.\n" +
                "\tPhenogrid.createPhenogridForElement(document.getElementById('phenogrid_container'), {\n" +
                    "\t\tserverURL : \"https://monarchinitiative.org\",\n" +
                    "\t\tgridSkeletonData: data\n" +
                    "\t});\n" +
                "}\n" +
                "</script>\n" +
                "\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\n" +
                "<div id=\"phenogrid_container\" class=\"clearfix\"></div>\n" +
                "\n" +
                "</body>\n" +
                "</html>"
            );
            bPhenogrid.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
