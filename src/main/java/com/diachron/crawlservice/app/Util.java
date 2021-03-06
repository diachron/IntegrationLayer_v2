package com.diachron.crawlservice.app;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import management.configuration.PropertiesManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import java.util.UUID;
import java.io.PrintWriter;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rdfization.RDFConverter;


public final class Util
{
    private final Properties prop = new Properties();

    public static String getCrawlid(URL urltoCrawl) {
        String crawlid = "";
        System.out.println("start crawling page");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        PropertiesManager props = PropertiesManager.getPropertiesManager();
        System.out.println(AuthScope.ANY_HOST + " " + AuthScope.ANY_PORT + " " + props.getPropertyValue("REMOTE_CRAWLER_USERNAME") + " " +
                props.getPropertyValue("REMOTE_CRAWLER_PASS"));
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(props.getPropertyValue("REMOTE_CRAWLER_USERNAME"), props.getPropertyValue("REMOTE_CRAWLER_PASS")));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {
            //HttpPost httppost = new HttpPost("http://diachron.hanzoarchives.com/crawl");
            HttpPost httppost = new HttpPost(PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_URL_CRAWL_INIT"));

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("name", UUID.randomUUID().toString()));
            urlParameters.add(new BasicNameValuePair("scope", "page"));
            urlParameters.add(new BasicNameValuePair("seed", urltoCrawl.toString()));

            httppost.setEntity(new UrlEncodedFormEntity(urlParameters));

            System.out.println("Executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println("----------------------------------------");

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    crawlid = inputLine;
                }
                in.close();
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return crawlid;
    }

    public static String getCrawlStatusById(String crawlid) {

        String status = "";
        System.out.println("get crawlid");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials( PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_USERNAME"), 
                        PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_PASS")));

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {
            HttpGet httpget = new HttpGet(PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_URL_CRAWL") + crawlid);

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println("----------------------------------------");

                String result = "";

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    result += inputLine;
                }
                in.close();

                // TO-DO should be removed in the future and handle it more gracefully
                result = result.replace("u'", "'");
                result = result.replace("'", "\"");

                System.err.println(" Configuration.REMOTE_CRAWLER_USERNAME=" + PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_USERNAME") + " "  + PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_USERNAME"));
                
                JSONObject crawljson = new JSONObject(result);
                System.out.println("myObject " + crawljson.toString());

                status = crawljson.getString("status");

                EntityUtils.consume(response.getEntity());
            } catch (JSONException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return status;
    }

    public static JSONArray getwarcsByCrawlid(String crawlid) {

        JSONArray warcsArray = null;
        System.out.println("get crawlid");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_USERNAME"), 
                        PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_PASS")));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {

            PropertiesManager pros = PropertiesManager.getPropertiesManager();
            HttpGet httpget = new HttpGet(pros.getPropertyValue("REMOTE_CRAWLER_URL") + crawlid);

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println("----------------------------------------");

                String result = "";

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    result += inputLine;
                }
                in.close();

                result = result.replace("u'", "'");
                result = result.replace("'", "\"");

                warcsArray = new JSONArray(result);

                for (int i = 0; i < warcsArray.length(); i++) {

                    System.out.println("url to download: " + warcsArray.getString(i));

                }

                EntityUtils.consume(response.getEntity());
            } catch (JSONException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return warcsArray;
    }

    private static String getwarcByURL(String warcURLasString) {
        File warcfile = null;
        try {
            URL warcURL = new URL(warcURLasString);
            String fileName = FilenameUtils.getBaseName(warcURLasString);

            warcfile = new File(PropertiesManager.getPropertiesManager().getPropertyValue("TMP_FOLDER_CRAWL") + fileName + ".gz");
            FileUtils.copyURLToFile(warcURL, warcfile);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("warcfile.getAbsolutePath() " + warcfile.getAbsolutePath());
        return warcfile.getAbsolutePath();

    }

    public static JSONArray manageWarcFile(String warcfilepath) {
        String downloadedfile = getwarcByURL(warcfilepath);

        JSONArray json4RDFizing = RDFizeWarcFile(downloadedfile);

        return json4RDFizing;
    }

    private static JSONArray RDFizeWarcFile(String warcfilepath) {
        JSONArray json4RDFizing = new JSONArray();
        FileInputStream is = null;        
        FileInputStream fis = null;
        String inPath  = new String("/home/panos/Downloads/test/small2.rdf");
        String outPath = new String("/home/panos/Downloads/test/small_efo.rdf.sample");     
                
        try {
            PrintWriter pw = new PrintWriter(inPath);
            
            String fileName = null;
            // Set up a local compressed WARC file for reading
            is = new FileInputStream(warcfilepath);
            // The file name identifies the ArchiveReader and indicates if it should be decompressed
            ArchiveReader ar = WARCReaderFactory.get(warcfilepath, is, true);
            // Once we have an ArchiveReader, we can work through each of the records it contains
            int i = 0;
            for (ArchiveRecord r : ar) {
                // The header file contains information such as the type of record, size, creation time, and URL
                //System.out.println(r.getHeader());
                byte[] rawData = IOUtils.toByteArray(r, r.available());
                String content = new String(rawData);
                Document doc = Jsoup.parse(content);
                Elements el = doc.select("a");
                
                if(el.size()>0)
                {
                    pw.println("<rdf:RDF\n" +
                                "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                                "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
                                "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">");

                    pw.println("<rdf:Description rdf:about=\"http://rand/" + UUID.randomUUID().toString() + "\">");
                    System.err.println( " -- " + el.size());


                    for(int j=0;j<el.size();j++)
                    {
                        System.err.println( " -- " + el.get(j).attr("href"));                    
                        pw.println("<rdfs:comment>" + el.get(j).attr("href") + "</rdfs:comment>");
                        pw.flush();
                    }

                    pw.println("</rdf:Description></rdf:RDF>");
                    pw.close();

                    fis = new FileInputStream(inPath);
                    FileOutputStream fos = new FileOutputStream(outPath);            

                    RDFConverter conv = new RDFConverter();
                    conv.convert(fis, fos, "testesttest", "rdf");
                    conv = null;                               
                }

                if (i++ > 4) {
                    break;
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return json4RDFizing;

    }

    public static String getTextBetweenTags(String content, String tag) {
        //System.err.println(" ----- BOOM " + content);
        final Pattern pattern = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">");
        final List<String> tagValues = new ArrayList<String>();
        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues.get(0);

    }

    public static boolean storeRDFizedWarcFile(Model model, String rdfizedWarcFilepath) {
        try {
            System.out.println("rdfizedWarcFilepath" + rdfizedWarcFilepath + "Configuration.TMP_SERIALIZATION_RDF_FORMAT " + PropertiesManager.getPropertiesManager().getPropertyValue("TMP_SERIALIZATION_RDF_FORMAT"));
            FileWriter outToSave = new FileWriter(rdfizedWarcFilepath);
            model.write(outToSave, PropertiesManager.getPropertiesManager().getPropertyValue("TMP_SERIALIZATION_RDF_FORMAT"));
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;

    }

    public static void getAllCrawls() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_USERNAME"), PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_PASS")));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {
            HttpGet httpget = new HttpGet(PropertiesManager.getPropertiesManager().getPropertyValue("REMOTE_CRAWLER_URL_CRAWL"));

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println("----------------------------------------");

                System.out.println(response.getEntity().getContent());

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                in.close();
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static String unzipWarcFile(String input_gzip_file) {

        String output_file = input_gzip_file.split(".gz")[0];

        byte[] buffer = new byte[1024];

        try {

            GZIPInputStream gzis
                    = new GZIPInputStream(new FileInputStream(input_gzip_file));

            FileOutputStream out = new FileOutputStream(output_file);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return output_file;
    }

    public static JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

    public static boolean generateRDFModel(JSONArray jsonArray4RDFizing, String crawlid) {

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();

        String base = "http://localhost:8181/Diacrawl";

        String ds = base + "#";
        String path = null;
        String crawl_base = base + "/" + crawlid;
        String crawl_base_ns = crawl_base + "#";

        model.setNsPrefix("ds", ds);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("cr", crawl_base_ns);

        Resource pageinfo_node = model.createResource(crawl_base_ns + "pageinfo");

        for (int i = 0; i < jsonArray4RDFizing.length(); i++) {

            try {
                JSONObject ob = jsonArray4RDFizing.getJSONObject(i);
                Resource pageinfo_node_statement = model.createResource(crawl_base_ns + "pageinfo/" + i);
                pageinfo_node_statement.addProperty(RDF.type, pageinfo_node);
                pageinfo_node_statement.addProperty(RDFS.label, ob.getString("title"));
                pageinfo_node_statement.addProperty(RDFS.isDefinedBy, ob.getString("headerURL"));
                pageinfo_node_statement.addProperty(RDFS.comment, ob.getString("firstParagraph"));
            } catch (JSONException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        /*
        path = PropertiesManager.getPropertiesManager().getPropertyValue("TMP_FOLDER_CRAWL")
                                  + crawlid + "RDFized."
                                  + PropertiesManager.getPropertiesManager().getPropertyValue("TMP_SERIALIZATION_RDF_FILEEXT");
        
        path= null;
        System.out.println(" ---------" + path);
        
        storeRDFizedWarcFile(model, path);
          */                        
        
        return true;
    }

    public static void main(String[] args) throws Exception {
//        String urlstring = "http://hanzoenterprise.s3.amazonaws.com/RaC/DIACHRON/7551fda7-08cf-4662-9bd6-670006d1e027/7551fda7-08cf-4662-9bd6-670006d1e027-crawl/7551fda7-08cf-4662-9bd6-670006d1e027-crawl-20150707131529-00000-560e5316-271e-44b3-9222-0c9ce38ff61c.warc.gz?Signature=hbg6ecctMldLf%2BwA7ldIRT77O4s%3D&Expires=1436347898&AWSAccessKeyId=0WTGAF2NBCANE1MSZQG2&x-amz-storage-class=REDUCED_REDUNDANCY";
//
//        getwarcByURL(urlstring);

//        URL a = new URL("http://example.com/");
//        String crawlid  = getCrawlid(a);
//        System.out.println("to crawl id mou einai "+crawlid);
//        String status = getCrawlStatusById("ca23f7a8-02ba-4a39-afe3-a03cc6f5ea67");
//        System.out.println("my status " + status);
//
//        JSONArray warcsArray = getwarcsByCrawlid("d0be3ec6-4566-4b6d-bb83-cf6cf95d1217");
//
//        for (int i = 0; i < warcsArray.length(); i++) {
//
//            System.out.println("url to download1: " + warcsArray.getString(i));
//
//        }
        String warcfile = "/home/panos/Downloads/ebi_sample.warc.gz";
        
        System.err.println("DATA:" + RDFizeWarcFile(warcfile));
        
        RDFizeWarcFile(warcfile);
        //String warcrdfFile = "/home/panos/Downloads/a374b6db-a50f-4295-9e09-6234b246246fRDFized.rdf";
        //DiachronizationModule conv = new DiachronizationModule();
        //conv.uploadDiachronizeAndStore("test", warcrdfFile, "/home/panos/Downloads/test.rdf", "label", "cre","ontology");
        
    }

}
