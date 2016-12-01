import com.mongodb.casbah.{MongoClient, MongoClientURI}
import scala.io.Source

/**
  * Created by avp on 11/30/2016.
  */
object DBConnection {

  val mongoURI = MongoClientURI("mongodb://admin:new_password@104.197.28.49:27017/admin");
  val mongoClient = MongoClient(mongoURI);
  val db = mongoClient("cs441project");

  def main(args: Array[String]): Unit = {
    readFile("./documents/example.json");
    insertRecord("movie", "");
    findAll("movie");

  }

  def insertRecord(collectionName:String, jsonRecord:String): Unit ={
    val collectionObject = db(collectionName);

  }

  def readFile(fileName:String): Unit ={
    for (line <- io.Source.fromFile(fileName).getLines()) {
      println(line)
    }
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
