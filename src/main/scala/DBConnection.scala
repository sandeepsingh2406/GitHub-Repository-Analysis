import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.mongodb.util.JSON;

import scala.io.Source

/**
  * Created by avp on 11/30/2016.
  */
object DBConnection {

  val mongoURI = MongoClientURI("mongodb://admin:new_password@104.197.28.49:27017/admin");
  val mongoClient = MongoClient(mongoURI);
  val db = mongoClient("cs441project");
  val collectionName = "movie";
  val sampleJSONPath = "./documents/example.json";

  def main(args: Array[String]): Unit = {
    val jsonRecordString = readFirstLineOfFile(sampleJSONPath);
    insertRecord(collectionName, createDBObject(jsonRecordString));
    findAll(collectionName);

  }

  // insert given object in given collection
  def insertRecord(collectionName:String, dbObject: DBObject): Unit ={
    val collectionObject = db(collectionName);
    collectionObject.insert(dbObject);
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
    }
  }
}
