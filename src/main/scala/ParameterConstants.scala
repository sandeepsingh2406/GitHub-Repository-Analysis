/**
  * Created by avp on 12/1/2016.
  */
object ParameterConstants {

  val mongoPrefix = "mongodb";
  val userName = "admin";
  val password = "new_password";
  val mongoDBHostIPAddress = "104.197.28.49";
  val dbPortNumber = "27017";
  val sampleRepoJSONPath = "./documents/example_repo.json";
  val sampleUserJSONPath = "./documents/example_user.json";
  // connection database name is used only for connecting to remote mongodb instance, as I have created a used only for
  // this database.
  val connectionDBName = "admin";
  // useageDB is the actual db bding used for stiring the
  val usageDBName = "cs441project";
  // collection names being used in the database
  val collectionNameSuffix = "Collection";
  val defaultCollectionName = "movie";
  val usersCollectionName = "usersCollection";
  val cCollectionName = "cCollection";
  val javaCollectionName = "javaCollection";
  val pythonCollectionName = "pythonCollection";
  val goCollectionName = "goCollection";

  // mysql related constants
  val mysqlDriver = "com.mysql.jdbc.Driver";
  val mysqlUserName = "root";
  val mysqlPassword = "new_password";
  val mysqlHostIPAddress = "104.155.94.91";
  val mysqlDBPortNumber = "3306";
  val mysqlDBName = "cs441project";
  val mysqlPrefix = "jdbc:mysql";
}
