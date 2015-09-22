/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package generaltesting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.athena.imis.diachron.archive.datamapping.OntologyConverter;

public class TestConversionFile {
/**
* @param args
*/
public static void main(String[] args)
{
    // TODO Auto-generated method stub
    String dir = "/home/panos/Downloads/test/";
    File[] files = new File(dir).listFiles();
    for(File file : files){
    System.out.println("Converting file " + file.getName());
    File inputFile = new File(dir+file.getName());
    FileInputStream fis = null;
    File outputFile = new File(dir+"_diachron_"+file.getName());

        try {
            fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            OntologyConverter converter = new OntologyConverter();                                   
            converter.convert(fis, fos, "test18");
            
            //MultidimensionalConverter converter = new MultidimensionalConverter();
            //converter.convert(fis, fos, file.getName().substring(file.getName().lastIndexOf(".")+1), "test_qb_data");
            
            
            fis.close();
            fos.close();

            
            //fis = new FileInputStream("/home/diachron/diachron_small_efo.rdf");
            //StoreFactory.createDataLoader().loadData(fis, "http://www.diachron-fp7.eu/resource/attribute/test/A1395204C1F346B32D541044F24E0E0C", "RDF/XML");

            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(TestConversionFile.class.getName()).log(Level.SEVERE, null, ex);
        }
}
}
}
