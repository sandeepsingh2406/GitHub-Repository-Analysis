import com.mongodb.DBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.mongodb.util.JSON

import scala.io.Source

/**
  * Created by avp on 11/30/2016.
  */
object MongoDBOperationAPIs {

//  val mongoURI = MongoClientURI("mongodb://admin:new_password@104.197.28.49:27017/admin");
  val mongoURI = MongoClientURI(ParameterConstants.mongoPrefix + "://" + ParameterConstants.userName + ":"
  + ParameterConstants.password + "@" + ParameterConstants.hostIPAddress + ":" + ParameterConstants.dbPortNumber +  "/"
  + ParameterConstants.connectionDBName);
  val mongoClient = MongoClient(mongoURI);
  val db = mongoClient(ParameterConstants.usageDBName);
  val collectionName = ParameterConstants.defaultCollectionName;
  val sampleJSONPath = ParameterConstants.sampleUserJSONPath;
//  val sampleJSONPath = ParameterConstants.sampleRepoJSONPath;

  def main(args: Array[String]): Unit = {
    val jsonRecordString = readFirstLineOfFile(sampleJSONPath);
    println("parsed json string: " + jsonRecordString);
//    insertDBObject(collectionName, createDBObject(jsonRecordString));
//    findAll(ParameterConstants.defaultCollectionName);
    println(getCollectionCount(ParameterConstants.cCollectionName));
  }

  // returns total number of documents in given collection
  def getCollectionCount(collectonName: String): Int = {
    return db(collectionName).count();
  }

  // insert jsonfile directly to given collection
  def insertFileJSON(collectionName:String, jsonFileName:String): Unit ={
    insertStringJSON(collectionName, readFirstLineOfFile(jsonFileName));
  }

  def insertStringJSONByID(): Unit = {

  }
  // insert string into given collection
  def insertStringJSON(collectionName:String, jsonRecordString:String): Unit ={
    insertDBObject(collectionName, createDBObject(jsonRecordString));
  }

  // insert given object in given collection
  def insertDBObject(collectionName:String, dbObject: DBObject): Unit ={
    db(collectionName).insert(dbObject);
  }

  // save object by specifying unique id
  def saveDBObjectByID(id:String, collectionName:String, dBObject: DBObject): Unit = {
//    db.movie.save({_id:"sdSd", "name":"test"});
//    db(collectionName).save({"_id":});
  }

  // take json string as input and return dbObject which can be inserted directly into MongoDB
  def createDBObject(jsonString: String): DBObject ={
    val dbObject = JSON.parse(jsonString).asInstanceOf[DBObject];
    return dbObject;
  }

  // read given fiel and return first line.
  def readFirstLineOfFile(fileName:String): String ={
    val source = Source.fromFile(fileName);
    var line: String = "";
    try{
      line = source.getLines().next()
    }
    catch{
      case e: Exception => {println("Exception while reading file. " + e.printStackTrace())}
    }
    return line;
  }

  // given a collectionName, print all the entries of that collection
  def findAll(collectionName: String): Unit = {
    val collectionObject = db(collectionName);

    collectionObject.find();

    for(x <- collectionObject){
      println("value: " + x);
      println(x.getClass)
    }
  }
}
