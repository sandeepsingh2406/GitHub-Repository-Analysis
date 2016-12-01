import com.mongodb.casbah.{MongoClient, MongoClientURI}

/**
  * Created by avp on 11/30/2016.
  */
object DBConnection {

  val mongoURI = MongoClientURI("mongodb://admin:new_password@104.197.28.49:27017/admin");
  val mongoClient = MongoClient(mongoURI);
  val db = mongoClient("cs441project");

  def main(args: Array[String]): Unit = {
    findAll("movie");

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
