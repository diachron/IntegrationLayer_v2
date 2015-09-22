
package com.diachron.crawlservice.scheduler;

import com.diachron.crawlservice.activemq.CrawlTopicProducer;
import static generaltesting.TestJmsApp.thread;
import com.diachron.crawlservice.app.Util;
import java.util.logging.Level;
import java.util.logging.Logger;
import management.configuration.PropertiesManager;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;

public class CrawlStatusJobListener implements JobListener
{

    public static final String LISTENER_NAME = "crawlStatusJobListener";

    @Override
    public String getName() {
        return LISTENER_NAME; //must return a name
    }

    // Run this if job is about to be executed.
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

        String jobName = context.getJobDetail().getKey().toString();
        System.out.println("jobToBeExecuted");
        System.out.println("Job : " + jobName + " is going to start...");

    }

    //Run this after job has been executed
    @Override
    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException) {
        try {

            JobDataMap data = context.getJobDetail().getJobDataMap();
            String crawlid = data.getString("CRAWL_ID");
            String status = data.getString("status");
            String jobName = context.getJobDetail().getKey().toString();

            System.out.println("Job was executed : " + jobName + " with status: " + status);

            if (status.equalsIgnoreCase("complete")) {

                //get warcs 
                JSONArray warcsArray = Util.getwarcsByCrawlid(crawlid);

                JSONArray jsonArray4RDFizing = new JSONArray();

                for (int i = 0; i < warcsArray.length(); i++) {

                    System.out.println("warc_url to download: " + warcsArray.getString(i));

                    JSONArray json = Util.manageWarcFile(warcsArray.getString(i));
                    jsonArray4RDFizing = Util.concatArray(jsonArray4RDFizing, json);

                }

                System.out.println("jsonArray4RDFizing FINAL" + jsonArray4RDFizing);
                boolean issaved = Util.generateRDFModel(jsonArray4RDFizing, crawlid);

                //sent message to Topic
                System.out.println("topicName from Producer " + crawlid);
                PropertiesManager properties = PropertiesManager.getPropertiesManager();
                String BROKER_URL = properties.getPropertyValue("BROKER_URL");
                System.out.println("Configuration.BROKER_URL" + properties.getPropertyValue("BROKER_URL"));
                CrawlTopicProducer producer = new CrawlTopicProducer(BROKER_URL, crawlid);
                thread(producer, false);
                //deledeJob from scheduler context
                context.getScheduler().deleteJob(context.getJobDetail().getKey());

            }
            System.out.println("------------------The end-----------------------");


        } catch (SchedulerException ex) {
            Logger.getLogger(CrawlStatusJobListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CrawlStatusJobListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
