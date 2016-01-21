Instruction for the config file of the integrated platform

Create in $WILDFLY_HOME/modules/ 3 folders one inside of the other as "/diachron/configuration/main/".

Inside "main" folder create module.xml file as follows:
```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.1" name="diachron.configuration">
  <resources>
    <resource-root path="."/>
  </resources>
</module>
```
Inside "main" folder create diachron.properties file as follows (change adequately the properties):

--------------------------------
```
BaseServer                              = http://127.0.0.1:7090/DIACHRONIntegrationLayer/webresources
ComputeQualityAssementResourceAddress   = http://localhost:8080/Luzzu/compute_quality
ChangeDetectionResourceAddress          = http://192.168.3.8:8080/ForthMaven-1.0/diachron/change_detection
ChangeDetectionQueryInterface           = http://192.168.3.8:8080/ForthMaven-1.0/diachron/change_detection/query
ArchiveResourceAddress                  = http://192.168.3.8:8080/archive-web-services/archive
ArchiveDatasetResourceAddress           = http://192.168.3.8:8080/archive-web-services/archive/dataset
DiachronicURL                           = http://www.diachron-fp7.eu/resource/diachronicDataset/
ConverterType                           = ontology

DataAccessCoreSPARQlEndpointURL         = http://83.212.99.102:8890/sparql

ServerTempFolder                 = /home/diachron/temp/

DefaultBrokerURL                 = failover://tcp://localhost:61616

QualityAssessmentMonoQueue       = QUALITYASSESSMENTMONOQUEUE
ComplexDetectionDispatcherQueue  = COMPLEXDETECTIONDISPATCHERQUEUE
ComplexQueryDispatcherQueue      = COMPLEXQUERYDISPATCHERQUEUE
UnqueueDefaultWaiting            = 2000

BROKER_URL = tcp://127.0.0.1:61616?jms.prefetchPolicy.all=1000
REMOTE_CRAWLER_URL = http://diachron.hanzoarchives.com/warcs/
REMOTE_CRAWLER_URL_CRAWL_INIT = http://diachron.hanzoarchives.com/crawl
REMOTE_CRAWLER_URL_CRAWL = http://diachron.hanzoarchives.com/crawl/
REMOTE_CRAWLER_USERNAME = diachron
REMOTE_CRAWLER_PASS = 7nD9dNGshTtficn
TMP_FOLDER_CRAWL = /home/diachron/temp/
TMP_SERIALIZATION_RDF_FORMAT  = RDF/XML
TMP_SERIALIZATION_RDF_FILEEXT = rdf

Repository_IP=192.168.3.8
Repository_Username=dba
Repository_Password=dba
Repository_Port=1111
Dataset_URI = http://www.ebi.ac.uk/efo/
Simple_Changes_Folder=/home/diachron/diachron_v2/detection_repair_maven/sparql/ontological/simple_changes/with_assoc
Simple_Changes=ADD_COMMENT,DELETE_COMMENT,ADD_LABEL,ADD_TYPE_CLASS,ADD_TYPE_PROPERTY,ADD_SUPERCLASS,ADD_PROPERTY_INSTANCE,ADD_TYPE_TO_INDIVIDUAL,ADD_DOMAIN,ADD_RANG$
Validation_Dataset=http://repair/test
Validation_Ontology=http://dbpedia.org/ontology/3.6

Changes_Ontology_File = /home/diachron/detection_backend/input/changes_ontology/ontological/ChangesOntologySchema.n3
```
